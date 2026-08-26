package com.jovanmosurovic.personalfinancemanager.ui

import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionNameFormatter
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType

internal enum class TransactionTypeFilter(val labelRes: Int) {
    ALL(R.string.filter_type_all),
    EXPENSE(R.string.add_expense),
    INCOME(R.string.add_income)
}

internal const val ALL_CATEGORIES_FILTER = Long.MIN_VALUE

internal data class TransactionFilterCriteria(
    val searchQuery: String = "",
    val type: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val categoryId: Long = ALL_CATEGORIES_FILTER,
    val dateEpochDay: Long? = null
) {
    private val normalizedQuery = searchQuery.trim()

    fun matches(transaction: TransactionEntity): Boolean {
        val matchesSearch = normalizedQuery.isBlank() ||
            transaction.merchant.contains(normalizedQuery, ignoreCase = true) ||
            TransactionNameFormatter.displayName(transaction.merchant)
                .contains(normalizedQuery, ignoreCase = true) ||
            transaction.note.contains(normalizedQuery, ignoreCase = true)
        val matchesType = when (type) {
            TransactionTypeFilter.ALL -> true
            TransactionTypeFilter.EXPENSE -> transaction.type == TransactionType.EXPENSE.name
            TransactionTypeFilter.INCOME -> transaction.type == TransactionType.INCOME.name
        }
        val matchesCategory = categoryId == ALL_CATEGORIES_FILTER ||
            transaction.categoryId == categoryId
        val matchesDate = dateEpochDay == null || transaction.dateEpochDay == dateEpochDay
        return matchesSearch && matchesType && matchesCategory && matchesDate
    }
}

internal fun filterTransactions(
    transactions: List<TransactionEntity>,
    criteria: TransactionFilterCriteria
): List<TransactionEntity> = transactions.filter(criteria::matches)

internal fun sortTransactions(transactions: List<TransactionEntity>): List<TransactionEntity> =
    transactions.sortedWith(
        compareByDescending<TransactionEntity> { it.dateEpochDay }
            .thenByDescending { it.id }
    )
