package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate
import kotlin.math.absoluteValue

internal enum class AnalyticsPeriod(val labelRes: Int) {
    LAST_7_DAYS(R.string.analytics_period_7_days),
    LAST_30_DAYS(R.string.analytics_period_30_days),
    THIS_MONTH(R.string.analytics_period_this_month)
}

internal data class AnalyticsDateRange(
    val start: LocalDate,
    val end: LocalDate
)

internal data class SpendingPoint(
    val date: LocalDate,
    val amountMinor: Long
)

internal data class CategorySpending(
    val categoryId: Long?,
    val amountMinor: Long
)

internal fun AnalyticsPeriod.dateRange(today: LocalDate): AnalyticsDateRange = when (this) {
    AnalyticsPeriod.LAST_7_DAYS -> AnalyticsDateRange(today.minusDays(6), today)
    AnalyticsPeriod.LAST_30_DAYS -> AnalyticsDateRange(today.minusDays(29), today)
    AnalyticsPeriod.THIS_MONTH -> AnalyticsDateRange(today.withDayOfMonth(1), today)
}

internal fun spendingPointsForPeriod(
    transactions: List<TransactionEntity>,
    period: AnalyticsPeriod
): List<SpendingPoint> {
    val range = period.dateRange(LocalDate.now())
    val expenseByDay = transactions
        .asSequence()
        .filter { it.type == TransactionType.EXPENSE.name }
        .groupBy { it.dateEpochDay }
        .mapValues { (_, dayTransactions) -> dayTransactions.sumOf { it.amountMinor } }

    return generateSequence(range.start) { date ->
        date.plusDays(1).takeIf { !it.isAfter(range.end) }
    }.map { date ->
        SpendingPoint(date, expenseByDay[date.toEpochDay()] ?: 0L)
    }.toList()
}

internal fun categorySpendingForPeriod(
    transactions: List<TransactionEntity>,
    period: AnalyticsPeriod
): List<CategorySpending> {
    val range = period.dateRange(LocalDate.now())
    val start = range.start.toEpochDay()
    val end = range.end.toEpochDay()

    return transactions
        .asSequence()
        .filter { it.type == TransactionType.EXPENSE.name && it.dateEpochDay in start..end }
        .groupBy { it.categoryId }
        .map { (categoryId, categoryTransactions) ->
            CategorySpending(categoryId, categoryTransactions.sumOf { it.amountMinor })
        }
        .sortedByDescending { it.amountMinor }
}

@Composable
internal fun analyticsPeriodLabel(period: AnalyticsPeriod): String =
    stringResource(period.labelRes)

internal fun percentageChange(current: Long, previous: Long): String {
    if (previous == 0L) return "0%"
    val percentage = ((current - previous).toDouble() / previous.toDouble()) * 100.0
    return "${percentage.absoluteValue.toInt()}%"
}
