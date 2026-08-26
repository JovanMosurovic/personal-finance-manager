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
    }

    @Test
    fun spendingPointsSumOnlyExpensesAndKeepEmptyDays() {
        val today = LocalDate.now()
        val transactions = listOf(
            transaction(id = 1, date = today, amountMinor = 100),
            transaction(id = 2, date = today, amountMinor = 250),
            transaction(
                id = 3,
                date = today,
                amountMinor = 900,
                type = TransactionType.INCOME
            )
        )

        val points = spendingPointsForPeriod(transactions, AnalyticsPeriod.LAST_7_DAYS)

        assertEquals(7, points.size)
        assertEquals(350L, points.last().amountMinor)
        assertEquals(0L, points[points.lastIndex - 1].amountMinor)
    }

    @Test
    fun categorySpendingGroupsOnlyCurrentPeriodExpenses() {
        val today = LocalDate.now()
        val transactions = listOf(
            transaction(id = 1, date = today, amountMinor = 100, categoryId = 10),
            transaction(id = 2, date = today, amountMinor = 200, categoryId = 10),
            transaction(id = 3, date = today, amountMinor = 400, categoryId = 20),
            transaction(id = 4, date = today, amountMinor = 500, categoryId = null),
            transaction(
                id = 5,
                date = today,
                amountMinor = 1_000,
                categoryId = 10,
                type = TransactionType.INCOME
            ),
            transaction(id = 6, date = today.minusMonths(2), amountMinor = 900, categoryId = 30)
        )

        assertEquals(
            listOf(
                CategorySpending(categoryId = null, amountMinor = 500),
                CategorySpending(categoryId = 20, amountMinor = 400),
                CategorySpending(categoryId = 10, amountMinor = 300)
            ),
            categorySpendingForPeriod(transactions, AnalyticsPeriod.THIS_MONTH)
        )
    }

    @Test
    fun percentageChangeUsesAbsoluteDifference() {
        assertEquals("25%", percentageChange(current = 75, previous = 100))
        assertEquals("0%", percentageChange(current = 100, previous = 0))
    }

    private fun transaction(
        id: Long,
        date: LocalDate,
        amountMinor: Long,
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
