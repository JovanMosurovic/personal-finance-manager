package com.jovanmosurovic.personalfinancemanager.ui

import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionNameFormatter
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate

internal enum class TransactionTypeFilter(val labelRes: Int) {
    ALL(R.string.filter_type_all),
    EXPENSE(R.string.add_expense),
    INCOME(R.string.add_income)
}

internal enum class TransactionDateFilter(val labelRes: Int) {
    ALL_TIME(R.string.filter_period_all),
    LAST_7_DAYS(R.string.analytics_period_7_days),
    LAST_30_DAYS(R.string.analytics_period_30_days),
    THIS_MONTH(R.string.analytics_period_this_month),
    MONTH(R.string.filter_period_month),
    CUSTOM(R.string.filter_period_custom)
}

internal enum class TransactionSort(val labelRes: Int) {
    NEWEST(R.string.sort_newest),
    OLDEST(R.string.sort_oldest),
    HIGHEST_AMOUNT(R.string.sort_highest_amount),
    LOWEST_AMOUNT(R.string.sort_lowest_amount)
}

internal const val ALL_CATEGORIES_FILTER = Long.MIN_VALUE
internal const val UNCATEGORIZED_FILTER = 0L

internal data class TransactionFilterCriteria(
    val searchQuery: String = "",
    val type: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val categoryId: Long = ALL_CATEGORIES_FILTER,
    val date: TransactionDateFilter = TransactionDateFilter.ALL_TIME,
    val selectedMonthEpochDay: Long = LocalDate.now().withDayOfMonth(1).toEpochDay(),
    val customStartEpochDay: Long? = null,
    val customEndEpochDay: Long? = null,
    val today: LocalDate = LocalDate.now()
) {
    private val normalizedQuery = searchQuery.trim()

    fun matches(transaction: TransactionEntity): Boolean {
        return matchesSearch(transaction) &&
            matchesType(transaction) &&
            matchesCategory(transaction) &&
            matchesDate(transaction)
    }

    private fun matchesSearch(transaction: TransactionEntity): Boolean {
        if (normalizedQuery.isBlank()) return true

        return transaction.merchant.contains(normalizedQuery, ignoreCase = true) ||
            TransactionNameFormatter.displayName(transaction.merchant)
                .contains(normalizedQuery, ignoreCase = true) ||
            transaction.note.contains(normalizedQuery, ignoreCase = true)
    }

    private fun matchesType(transaction: TransactionEntity): Boolean {
        return when (type) {
            TransactionTypeFilter.ALL -> true
            TransactionTypeFilter.EXPENSE -> transaction.type == TransactionType.EXPENSE.name
            TransactionTypeFilter.INCOME -> transaction.type == TransactionType.INCOME.name
        }
    }

    private fun matchesCategory(transaction: TransactionEntity): Boolean {
        return when (categoryId) {
            ALL_CATEGORIES_FILTER -> true
            UNCATEGORIZED_FILTER -> transaction.categoryId == null
            else -> transaction.categoryId == categoryId
        }
    }

    private fun matchesDate(transaction: TransactionEntity): Boolean {
        val transactionDate = transaction.dateEpochDay
        val todayEpochDay = today.toEpochDay()

        return when (date) {
            TransactionDateFilter.ALL_TIME -> true
            TransactionDateFilter.LAST_7_DAYS -> {
                transactionDate in today.minusDays(6).toEpochDay()..todayEpochDay
            }
            TransactionDateFilter.LAST_30_DAYS -> {
                transactionDate in today.minusDays(29).toEpochDay()..todayEpochDay
            }
            TransactionDateFilter.THIS_MONTH -> {
                transactionDate in today.withDayOfMonth(1).toEpochDay()..todayEpochDay
            }
            TransactionDateFilter.MONTH -> {
                val month = LocalDate.ofEpochDay(selectedMonthEpochDay).withDayOfMonth(1)
                val monthEnd = month.withDayOfMonth(month.lengthOfMonth())
                transactionDate in month.toEpochDay()..monthEnd.toEpochDay()
            }
            TransactionDateFilter.CUSTOM -> {
                customStartEpochDay != null &&
                    customEndEpochDay != null &&
                    transactionDate in customStartEpochDay..customEndEpochDay
            }
        }
    }
}

internal fun filterTransactions(
    transactions: List<TransactionEntity>,
    criteria: TransactionFilterCriteria
): List<TransactionEntity> = transactions.filter(criteria::matches)

internal fun sortTransactions(
    transactions: List<TransactionEntity>,
    sort: TransactionSort
): List<TransactionEntity> = when (sort) {
    TransactionSort.NEWEST -> transactions.sortedWith(
        compareByDescending<TransactionEntity> { it.dateEpochDay }
            .thenByDescending { it.id }
    )
    TransactionSort.OLDEST -> transactions.sortedWith(
        compareBy<TransactionEntity> { it.dateEpochDay }
            .thenBy { it.id }
    )
    TransactionSort.HIGHEST_AMOUNT -> transactions.sortedWith(
        compareByDescending<TransactionEntity> { it.amountMinor }
            .thenByDescending { it.dateEpochDay }
            .thenByDescending { it.id }
    )
    TransactionSort.LOWEST_AMOUNT -> transactions.sortedWith(
        compareBy<TransactionEntity> { it.amountMinor }
            .thenByDescending { it.dateEpochDay }
            .thenByDescending { it.id }
    )
}
