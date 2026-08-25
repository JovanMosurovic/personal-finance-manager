package com.jovanmosurovic.personalfinancemanager.ui

import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.absoluteValue

internal enum class AnalyticsPeriod(
    val labelRes: Int
) {
    LAST_7_DAYS(R.string.analytics_period_7_days),
    LAST_30_DAYS(R.string.analytics_period_30_days),
    THIS_MONTH(R.string.analytics_period_this_month),
    CUSTOM(R.string.filter_period_custom)
}

internal enum class AnalyticsCustomPeriodMode {
    WHOLE_MONTH,
    DATE_RANGE
}

internal enum class AnalyticsComparisonMode(
    val labelRes: Int
) {
    SAME_DURATION(R.string.analytics_same_duration),
    PREVIOUS_MONTH(R.string.analytics_previous_month),
    SELECTED_MONTH(R.string.analytics_selected_month)
}

internal data class AnalyticsDateRange(
    val start: LocalDate,
    val end: LocalDate
) {
    val dayCount: Int
        get() = ChronoUnit.DAYS.between(start, end).toInt() + 1
}

internal data class SpendingPoint(
    val date: LocalDate,
    val amountMinor: Long
)

internal fun spendingPointsForPeriod(
    transactions: List<TransactionEntity>,
    period: AnalyticsPeriod,
    range: AnalyticsDateRange? = null
): List<SpendingPoint> {
    val today = LocalDate.now()
    val resolvedRange = range ?: period.dateRange(today)
    val expenseByDay = transactions
        .filter { it.type == TransactionType.EXPENSE.name }
        .groupBy { it.dateEpochDay }
        .mapValues { (_, dailyTransactions) ->
            dailyTransactions.sumOf { it.amountMinor }
        }

    return (0 until resolvedRange.dayCount).map { dayOffset ->
        val date = resolvedRange.start.plusDays(dayOffset.toLong())
        SpendingPoint(
            date = date,
            amountMinor = expenseByDay[date.toEpochDay()] ?: 0L
        )
    }
}

internal fun chartDayLabel(date: LocalDate, useDate: Boolean): String = if (useDate) {
    DateTimeFormatter.ofPattern("d.M.", Locale.getDefault()).format(date)
} else {
    DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        .format(date)
        .replace(".", "")
        .take(3)
        .uppercase(Locale.getDefault())
}

internal fun AnalyticsPeriod.dateRange(
    today: LocalDate,
    customRange: AnalyticsDateRange? = null
): AnalyticsDateRange = when (this) {
    AnalyticsPeriod.LAST_7_DAYS -> AnalyticsDateRange(
        start = today.minusDays(6),
        end = today
    )
    AnalyticsPeriod.LAST_30_DAYS -> AnalyticsDateRange(
        start = today.minusDays(29),
        end = today
    )
    AnalyticsPeriod.THIS_MONTH -> AnalyticsDateRange(
        start = today.withDayOfMonth(1),
        end = today
    )
    AnalyticsPeriod.CUSTOM -> customRange ?: AnalyticsDateRange(
        start = today.withDayOfMonth(1),
        end = today
    )
}

internal fun AnalyticsPeriod.comparisonRange(
    today: LocalDate,
    mode: AnalyticsComparisonMode,
    selectedMonth: LocalDate = today.minusMonths(1).withDayOfMonth(1),
    currentRange: AnalyticsDateRange? = null
): AnalyticsDateRange {
    return when (mode) {
        AnalyticsComparisonMode.PREVIOUS_MONTH -> fullMonthRange(today.minusMonths(1))
        AnalyticsComparisonMode.SELECTED_MONTH -> fullMonthRange(selectedMonth)
        AnalyticsComparisonMode.SAME_DURATION -> {
            if (this == AnalyticsPeriod.THIS_MONTH) {
                samePartOfPreviousMonth(today)
            } else {
                previousRangeWithSameDuration(currentRange ?: dateRange(today))
            }
        }
    }
}

private fun fullMonthRange(date: LocalDate): AnalyticsDateRange {
    val monthStart = date.withDayOfMonth(1)
    return AnalyticsDateRange(
        start = monthStart,
        end = monthStart.withDayOfMonth(monthStart.lengthOfMonth())
    )
}

private fun samePartOfPreviousMonth(today: LocalDate): AnalyticsDateRange {
    val previousMonth = today.minusMonths(1)
    val endDay = minOf(today.dayOfMonth, previousMonth.lengthOfMonth())
    return AnalyticsDateRange(
        start = previousMonth.withDayOfMonth(1),
        end = previousMonth.withDayOfMonth(endDay)
    )
}

private fun previousRangeWithSameDuration(range: AnalyticsDateRange): AnalyticsDateRange {
    val comparisonEnd = range.start.minusDays(1)
    return AnalyticsDateRange(
        start = comparisonEnd.minusDays(range.dayCount.toLong() - 1),
        end = comparisonEnd
    )
}

internal fun availableMonthsUntil(
    transactions: List<TransactionEntity>,
    latestMonth: LocalDate
): List<LocalDate> {
    val firstTransactionMonth = transactions
        .asSequence()
        .map { LocalDate.ofEpochDay(it.dateEpochDay).withDayOfMonth(1) }
        .filter { !it.isAfter(latestMonth) }
        .minOrNull()
        ?: latestMonth.minusMonths(11)

    val months = mutableListOf<LocalDate>()
    var month = latestMonth
    while (!month.isBefore(firstTransactionMonth)) {
        months += month
        month = month.minusMonths(1)
    }
    return months
}

internal fun availableComparisonMonths(
    transactions: List<TransactionEntity>,
    today: LocalDate
): List<LocalDate> = availableMonthsUntil(
    transactions = transactions,
    latestMonth = today.withDayOfMonth(1).minusMonths(1)
)

internal fun transactionsInRange(
    transactions: List<TransactionEntity>,
    range: AnalyticsDateRange
): List<TransactionEntity> {
    val startEpochDay = range.start.toEpochDay()
    val endEpochDay = range.end.toEpochDay()
    return transactions.filter { it.dateEpochDay in startEpochDay..endEpochDay }
}

internal fun formatDateRange(range: AnalyticsDateRange): String =
    "${formatDate(range.start.toEpochDay())} - ${formatDate(range.end.toEpochDay())}"

internal fun percentageChange(current: Long, previous: Long): String {
    if (previous == 0L) return "0%"

    val percentage = ((current - previous).toDouble() / previous.toDouble()) * 100.0
    return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(percentage.absoluteValue) + "%"
}

internal data class CategorySpending(
    val categoryId: Long?,
    val amountMinor: Long
)

internal fun categorySpendingForPeriod(
    transactions: List<TransactionEntity>
): List<CategorySpending> {
    return transactions
        .asSequence()
        .filter { it.type == TransactionType.EXPENSE.name }
        .groupBy { it.categoryId }
        .map { (categoryId, categoryTransactions) ->
            CategorySpending(
                categoryId = categoryId,
                amountMinor = categoryTransactions.sumOf { it.amountMinor }
            )
        }
        .sortedByDescending { it.amountMinor }
}
