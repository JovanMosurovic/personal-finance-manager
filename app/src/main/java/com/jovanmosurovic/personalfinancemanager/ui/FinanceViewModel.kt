package com.jovanmosurovic.personalfinancemanager.ui

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jovanmosurovic.personalfinancemanager.data.FinanceRepository
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.data.importer.OtpImportFormat
import com.jovanmosurovic.personalfinancemanager.data.importer.OtpStatementImportException
import com.jovanmosurovic.personalfinancemanager.data.importer.OtpStatementImporter
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

data class FinanceUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val keywordRules: List<KeywordRuleEntity> = emptyList(),
    val isReady: Boolean = false
) {
    val totalBalanceMinor: Long
        get() = transactions.sumOf { transaction ->
            if (transaction.type == TransactionType.INCOME.name) {
                transaction.amountMinor
            } else {
                -transaction.amountMinor
            }
        }

    val incomeThisMonthMinor: Long
        get() = currentMonthTransactions
            .filter { it.type == TransactionType.INCOME.name }
            .sumOf { it.amountMinor }

    val expensesThisMonthMinor: Long
        get() = currentMonthTransactions
            .filter { it.type == TransactionType.EXPENSE.name }
            .sumOf { it.amountMinor }

    private val currentMonthTransactions: List<TransactionEntity>
        get() {
            val today = LocalDate.now()
            val firstDayOfMonth = today.withDayOfMonth(1).toEpochDay()
            return transactions.filter { it.dateEpochDay >= firstDayOfMonth }
    }
}

enum class ImportError {
    UNSUPPORTED_FILE,
    INVALID_FILE,
    NO_TRANSACTIONS,
    READ_FAILED
}

data class ImportSummary(
    val format: OtpImportFormat,
    val parsedCount: Int,
    val importedCount: Int,
    val duplicateCount: Int
)

data class ImportUiState(
    val isImporting: Boolean = false,
    val summary: ImportSummary? = null,
    val error: ImportError? = null
)

@HiltViewModel
class FinanceViewModel @Inject constructor(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {
    private val defaultsReady = MutableStateFlow(false)
    private val _importState = MutableStateFlow(ImportUiState())
    val importState: StateFlow<ImportUiState> = _importState.asStateFlow()
    private val statementImporter = OtpStatementImporter()

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.observeTransactions(),
        repository.observeCategories(),
        repository.observeKeywordRules(),
        defaultsReady
    ) { transactions, categories, keywordRules, isReady ->
        FinanceUiState(
            transactions = transactions,
            categories = categories,
            keywordRules = keywordRules,
            isReady = isReady
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FinanceUiState()
    )

    init {
        viewModelScope.launch {
            repository.seedDefaults()
            defaultsReady.value = true
        }
    }

    fun addTransaction(
        type: TransactionType,
        amountMinor: Long,
        merchant: String,
        note: String,
        dateEpochDay: Long
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                type = type,
                amountMinor = amountMinor,
                merchant = merchant,
                note = note,
                dateEpochDay = dateEpochDay
            )
        }
    }

    fun addKeyword(categoryId: Long, keyword: String) {
        viewModelScope.launch {
            repository.addKeyword(categoryId, keyword)
        }
    }

    fun deleteKeyword(keywordRuleId: Long) {
        viewModelScope.launch {
            repository.deleteKeyword(keywordRuleId)
        }
    }

    fun updateTransaction(
        transactionId: Long,
        type: TransactionType,
        amountMinor: Long,
        merchant: String,
        note: String,
        dateEpochDay: Long
    ) {
        viewModelScope.launch {
            repository.updateTransaction(
                transactionId = transactionId,
                type = type,
                amountMinor = amountMinor,
                merchant = merchant,
                note = note,
                dateEpochDay = dateEpochDay
            )
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
        }
    }

    fun addCategory(name: String, parentId: Long?) {
        viewModelScope.launch {
            repository.addCategory(name, parentId)
        }
    }

    fun renameCategory(categoryId: Long, name: String) {
        viewModelScope.launch {
            repository.renameCategory(categoryId, name)
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
        }
    }

    fun assignCategory(
        transactionId: Long,
        categoryId: Long,
        saveMerchantAsKeyword: Boolean
    ) {
        viewModelScope.launch {
            repository.assignCategory(
                transactionId = transactionId,
                categoryId = categoryId,
                saveMerchantAsKeyword = saveMerchantAsKeyword
            )
        }
    }

    fun importStatement(uri: Uri) {
        if (_importState.value.isImporting) return

        val applicationContext = getApplication<Application>()
        val contentResolver = applicationContext.contentResolver
        val format = OtpImportFormat.from(
            mimeType = contentResolver.getType(uri),
            fileName = contentResolver.queryDisplayName(uri) ?: uri.lastPathSegment
        )
        if (format == null) {
            _importState.value = ImportUiState(error = ImportError.UNSUPPORTED_FILE)
            return
        }

        viewModelScope.launch {
            _importState.value = ImportUiState(isImporting = true)
            try {
                val parsedTransactions = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { input ->
                        statementImporter.parse(
                            format = format,
                            input = input,
                            context = applicationContext
                        )
                    } ?: throw OtpStatementImportException(
                        reason = OtpStatementImportException.Reason.INVALID_FILE
                    )
                }

                if (parsedTransactions.isEmpty()) {
                    _importState.value = ImportUiState(error = ImportError.NO_TRANSACTIONS)
                    return@launch
                }

                val insertResult = withContext(Dispatchers.IO) {
                    repository.importTransactions(parsedTransactions)
                }
                _importState.value = ImportUiState(
                    summary = ImportSummary(
                        format = format,
                        parsedCount = parsedTransactions.size,
                        importedCount = insertResult.importedCount,
                        duplicateCount = insertResult.duplicateCount
                    )
                )
            } catch (exception: OtpStatementImportException) {
                _importState.value = ImportUiState(
                    error = when (exception.reason) {
                        OtpStatementImportException.Reason.INVALID_FILE -> ImportError.INVALID_FILE
                        OtpStatementImportException.Reason.NO_TRANSACTIONS -> ImportError.NO_TRANSACTIONS
                    }
                )
            } catch (_: Exception) {
                _importState.value = ImportUiState(error = ImportError.READ_FAILED)
            }
        }
    }

    fun clearImportState() {
        if (!_importState.value.isImporting) {
            _importState.value = ImportUiState()
        }
    }

}

private fun android.content.ContentResolver.queryDisplayName(uri: Uri): String? = query(
    uri,
    arrayOf(OpenableColumns.DISPLAY_NAME),
    null,
    null,
    null
)?.use { cursor ->
    if (cursor.moveToFirst()) {
        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
    } else {
        null
    }
}
