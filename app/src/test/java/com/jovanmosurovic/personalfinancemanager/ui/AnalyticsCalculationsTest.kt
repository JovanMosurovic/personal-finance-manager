package com.jovanmosurovic.personalfinancemanager.ui

import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsCalculationsTest {
    @Test
    fun dateRangesUseInclusiveCalendarPeriods() {
        val today = LocalDate.of(2026, 8, 25)
        val customRange = AnalyticsDateRange(
            start = LocalDate.of(2026, 4, 3),
            end = LocalDate.of(2026, 4, 8)
        )

        assertEquals(
            AnalyticsDateRange(today.minusDays(6), today),
            AnalyticsPeriod.LAST_7_DAYS.dateRange(today)
        )
        assertEquals(
            AnalyticsDateRange(today.minusDays(29), today),
            AnalyticsPeriod.LAST_30_DAYS.dateRange(today)
        )
        assertEquals(
            AnalyticsDateRange(today.withDayOfMonth(1), today),
            AnalyticsPeriod.THIS_MONTH.dateRange(today)
        )
        assertEquals(customRange, AnalyticsPeriod.CUSTOM.dateRange(today, customRange))
        assertEquals(6, customRange.dayCount)
    }

    @Test
    fun comparisonRangesRespectDurationAndWholeMonthModes() {
        val today = LocalDate.of(2026, 3, 31)
        val currentRange = AnalyticsDateRange(
            start = LocalDate.of(2026, 3, 10),
            end = LocalDate.of(2026, 3, 16)
        )

        assertEquals(
            AnalyticsDateRange(
                start = LocalDate.of(2026, 3, 3),
                end = LocalDate.of(2026, 3, 9)
            ),
            AnalyticsPeriod.CUSTOM.comparisonRange(
                today = today,
                mode = AnalyticsComparisonMode.SAME_DURATION,
                currentRange = currentRange
            )
        )
        assertEquals(
            AnalyticsDateRange(
                start = LocalDate.of(2026, 2, 1),
                end = LocalDate.of(2026, 2, 28)
            ),
            AnalyticsPeriod.THIS_MONTH.comparisonRange(
                today = today,
                mode = AnalyticsComparisonMode.SAME_DURATION
            )
        )
        assertEquals(
            AnalyticsDateRange(
                start = LocalDate.of(2026, 2, 1),
                end = LocalDate.of(2026, 2, 28)
            ),
            AnalyticsPeriod.LAST_7_DAYS.comparisonRange(
                today = today,
                mode = AnalyticsComparisonMode.PREVIOUS_MONTH
            )
        )
        assertEquals(
            AnalyticsDateRange(
                start = LocalDate.of(2024, 2, 1),
                end = LocalDate.of(2024, 2, 29)
            ),
            AnalyticsPeriod.CUSTOM.comparisonRange(
                today = today,
                mode = AnalyticsComparisonMode.SELECTED_MONTH,
                selectedMonth = LocalDate.of(2024, 2, 15)
            )
        )
    }

    @Test
    fun spendingPointsSumOnlyExpensesAndKeepDaysWithoutSpending() {
        val firstDay = LocalDate.of(2026, 8, 1)
        val range = AnalyticsDateRange(firstDay, firstDay.plusDays(2))
        val transactions = listOf(
            transaction(id = 1, date = firstDay, amountMinor = 100),
            transaction(id = 2, date = firstDay, amountMinor = 250),
            transaction(
                id = 3,
                date = firstDay,
                amountMinor = 900,
                type = TransactionType.INCOME
            ),
            transaction(id = 4, date = firstDay.plusDays(2), amountMinor = 500),
            transaction(id = 5, date = firstDay.plusDays(3), amountMinor = 1_000)
        )

        assertEquals(
            listOf(
                SpendingPoint(firstDay, 350),
                SpendingPoint(firstDay.plusDays(1), 0),
                SpendingPoint(firstDay.plusDays(2), 500)
            ),
            spendingPointsForPeriod(
                transactions = transactions,
                period = AnalyticsPeriod.CUSTOM,
                range = range
            )
        )
    }

    @Test
    fun transactionRangeSelectionIncludesBothBoundaries() {
        val start = LocalDate.of(2026, 8, 10)
        val end = LocalDate.of(2026, 8, 12)
        val transactions = listOf(
            transaction(id = 1, date = start.minusDays(1)),
            transaction(id = 2, date = start),
            transaction(id = 3, date = end),
            transaction(id = 4, date = end.plusDays(1))
        )

        assertEquals(
            listOf(2L, 3L),
            transactionsInRange(transactions, AnalyticsDateRange(start, end)).map { it.id }
        )
    }

    @Test
    fun categorySpendingGroupsExpensesAndSortsLargestTotalFirst() {
        val transactions = listOf(
            transaction(id = 1, amountMinor = 100, categoryId = 10),
            transaction(id = 2, amountMinor = 200, categoryId = 10),
            transaction(id = 3, amountMinor = 400, categoryId = 20),
            transaction(id = 4, amountMinor = 500, categoryId = null),
            transaction(
                id = 5,
                amountMinor = 1_000,
                categoryId = null,
                type = TransactionType.INCOME
            )
        )

        assertEquals(
            listOf(
                CategorySpending(categoryId = null, amountMinor = 500),
                CategorySpending(categoryId = 20, amountMinor = 400),
                CategorySpending(categoryId = 10, amountMinor = 300)
            ),
            categorySpendingForPeriod(transactions)
        )
    }

    @Test
    fun availableMonthsFormAContinuousRangeAndIgnoreFutureTransactions() {
        val latestMonth = LocalDate.of(2026, 8, 1)
        val transactions = listOf(
            transaction(id = 1, date = LocalDate.of(2026, 5, 20)),
            transaction(id = 2, date = LocalDate.of(2026, 7, 4)),
            transaction(id = 3, date = LocalDate.of(2026, 9, 2))
        )

        assertEquals(
            listOf(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 5, 1)
            ),
            availableMonthsUntil(transactions, latestMonth)
        )
        assertEquals(
            listOf(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 5, 1)
            ),
            availableComparisonMonths(transactions, LocalDate.of(2026, 8, 25))
        )
    }

    private fun transaction(
        id: Long,
        date: LocalDate = LocalDate.of(2026, 8, 1),
        amountMinor: Long = 100,
        categoryId: Long? = null,
        type: TransactionType = TransactionType.EXPENSE
    ) = TransactionEntity(
        id = id,
        type = type.name,
        amountMinor = amountMinor,
        merchant = "Merchant $id",
        dateEpochDay = date.toEpochDay(),
        categoryId = categoryId
    )
}
