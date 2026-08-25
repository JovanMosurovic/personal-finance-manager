package com.jovanmosurovic.personalfinancemanager.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jovanmosurovic.personalfinancemanager.data.FinanceRepository
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
        get() = totalThisMonth(TransactionType.INCOME)

    val expensesThisMonthMinor: Long
        get() = totalThisMonth(TransactionType.EXPENSE)

    private fun totalThisMonth(type: TransactionType): Long {
        val firstDayOfMonth = LocalDate.now().withDayOfMonth(1).toEpochDay()
        return transactions
            .filter { transaction ->
                transaction.dateEpochDay >= firstDayOfMonth &&
                    transaction.type == type.name
            }
            .sumOf { it.amountMinor }
    }
}

@HiltViewModel
class FinanceViewModel @Inject constructor(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )
    private val defaultsReady = MutableStateFlow(false)
    private val _importState = MutableStateFlow(ImportUiState())
    private val _areAmountsHidden = MutableStateFlow(
        preferences.getBoolean(KEY_HIDE_AMOUNTS, false)
    )
    val importState: StateFlow<ImportUiState> = _importState.asStateFlow()
    val areAmountsHidden: StateFlow<Boolean> = _areAmountsHidden.asStateFlow()
    private val statementImportService = StatementImportService(application, repository)

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

        val format = statementImportService.findFormat(uri)
        if (format == null) {
            _importState.value = ImportUiState(error = ImportError.UNSUPPORTED_FILE)
            return
        }

        viewModelScope.launch {
            _importState.value = ImportUiState(isImporting = true)
            _importState.value = statementImportService.import(uri, format)
        }
    }

    fun clearImportState() {
        if (!_importState.value.isImporting) {
            _importState.value = ImportUiState()
        }
    }

    fun setAmountsHidden(hidden: Boolean) {
        _areAmountsHidden.value = hidden
        preferences.edit {
            putBoolean(KEY_HIDE_AMOUNTS, hidden)
        }
    }

}

private const val PREFERENCES_NAME = "finance_preferences"
private const val KEY_HIDE_AMOUNTS = "hide_amounts"
