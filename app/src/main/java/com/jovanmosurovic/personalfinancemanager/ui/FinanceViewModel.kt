package com.jovanmosurovic.personalfinancemanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jovanmosurovic.personalfinancemanager.data.FinanceRepository
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

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

class FinanceViewModel(
    private val repository: FinanceRepository
) : ViewModel() {
    private val defaultsReady = MutableStateFlow(false)

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

    class Factory(
        private val repository: FinanceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
                return FinanceViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
