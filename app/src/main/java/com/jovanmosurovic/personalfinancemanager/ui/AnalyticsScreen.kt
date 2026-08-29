package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate

@Composable
internal fun AnalyticsScreen(
    uiState: FinanceUiState,
    areAmountsHidden: Boolean,
    onViewAllTransactions: (
        categoryId: Long?,
        typeFilter: TransactionTypeFilter,
        dateStartEpochDay: Long?,
        dateEndEpochDay: Long?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by rememberSaveable {
        mutableStateOf(AnalyticsPeriod.LAST_7_DAYS)
    }
    var comparisonMode by rememberSaveable {
        mutableStateOf(AnalyticsComparisonMode.SAME_DURATION)
    }
    var comparisonMonthEpochDay by rememberSaveable {
        mutableStateOf(
            LocalDate.now()
                .minusMonths(1)
                .withDayOfMonth(1)
                .toEpochDay()
        )
    }
    var showComparisonMonthPicker by rememberSaveable { mutableStateOf(false) }
    var customPeriodMode by rememberSaveable {
        mutableStateOf(AnalyticsCustomPeriodMode.WHOLE_MONTH)
    }
    var customMonthEpochDay by rememberSaveable {
        mutableStateOf(LocalDate.now().withDayOfMonth(1).toEpochDay())
    }
    var customStartEpochDay by rememberSaveable {
        mutableStateOf(LocalDate.now().withDayOfMonth(1).toEpochDay())
    }
    var customEndEpochDay by rememberSaveable {
        mutableStateOf(LocalDate.now().toEpochDay())
    }
    var showCustomPeriodOptions by rememberSaveable { mutableStateOf(false) }
    var showCustomMonthPicker by rememberSaveable { mutableStateOf(false) }
    var showCustomRangePicker by rememberSaveable { mutableStateOf(false) }
    var selectedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showCategoryDetails by rememberSaveable { mutableStateOf(false) }
    var selectedDateEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }
    val today = LocalDate.now()
    val customMonth = LocalDate.ofEpochDay(customMonthEpochDay).withDayOfMonth(1)
    val customRange = if (customPeriodMode == AnalyticsCustomPeriodMode.WHOLE_MONTH) {
        AnalyticsDateRange(
            start = customMonth,
            end = customMonth.withDayOfMonth(customMonth.lengthOfMonth())
        )
    } else {
        val start = LocalDate.ofEpochDay(customStartEpochDay)
        val end = LocalDate.ofEpochDay(customEndEpochDay)
        AnalyticsDateRange(
            start = minOf(start, end),
            end = maxOf(start, end)
        )
    }
    val currentRange = selectedPeriod.dateRange(
        today = today,
        customRange = customRange.takeIf { selectedPeriod == AnalyticsPeriod.CUSTOM }
    )
    val comparisonMonth = LocalDate.ofEpochDay(comparisonMonthEpochDay).withDayOfMonth(1)
    val comparisonMonths = availableComparisonMonths(uiState.transactions, today)
    val customMonths = availableMonthsUntil(
        transactions = uiState.transactions,
        latestMonth = today.withDayOfMonth(1)
    )
    val comparisonRange = selectedPeriod.comparisonRange(
        today = today,
        mode = comparisonMode,
        selectedMonth = comparisonMonth,
        currentRange = currentRange
    )
    val periodTransactions = transactionsInRange(
        transactions = uiState.transactions,
        range = currentRange
    )
    val previousPeriodTransactions = transactionsInRange(
        transactions = uiState.transactions,
        range = comparisonRange
    )
    val incomeInPeriod = periodTransactions
        .filter { it.type == TransactionType.INCOME.name }
        .sumOf { it.amountMinor }
    val expensesInPeriod = periodTransactions
        .filter { it.type == TransactionType.EXPENSE.name }
        .sumOf { it.amountMinor }
    val netInPeriod = incomeInPeriod - expensesInPeriod
    val previousIncome = previousPeriodTransactions
        .filter { it.type == TransactionType.INCOME.name }
        .sumOf { it.amountMinor }
    val previousExpenses = previousPeriodTransactions
        .filter { it.type == TransactionType.EXPENSE.name }
        .sumOf { it.amountMinor }
    val previousNet = previousIncome - previousExpenses
    val categorySpending = categorySpendingForPeriod(periodTransactions)
    val selectedDate = selectedDateEpochDay?.let(LocalDate::ofEpochDay)
    val selectedDayTransactions = selectedDate?.let { date ->
        periodTransactions
            .filter { it.dateEpochDay == date.toEpochDay() }
            .sortedWith(compareByDescending<TransactionEntity> { it.type == TransactionType.EXPENSE.name }
                .thenByDescending { it.id })
    }
    val selectedCategory = uiState.categories.firstOrNull { it.id == selectedCategoryId }
    val selectedCategoryTransactions = if (showCategoryDetails) {
        periodTransactions
            .filter { it.categoryId == selectedCategoryId }
            .sortedWith(compareByDescending<TransactionEntity> { it.dateEpochDay }
                .thenByDescending { it.id })
    } else {
        emptyList()
    }
    val customPeriodLabel = if (customPeriodMode == AnalyticsCustomPeriodMode.WHOLE_MONTH) {
        formatMonthYear(customMonth)
    } else {
        formatDateRange(customRange)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = stringResource(R.string.analytics_title),
            style = MaterialTheme.typography.headlineLarge
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            AnalyticsPeriod.entries.forEach { period ->
                item(key = period.name) {
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = {
                            selectedPeriod = period
                            comparisonMode = AnalyticsComparisonMode.SAME_DURATION
                            selectedDateEpochDay = null
                            showCategoryDetails = false
                            if (period == AnalyticsPeriod.CUSTOM) {
                                showCustomPeriodOptions = true
                            }
                        },
                        label = { Text(stringResource(period.labelRes)) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        if (selectedPeriod == AnalyticsPeriod.CUSTOM) {
            OutlinedButton(
                onClick = { showCustomPeriodOptions = true },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = customPeriodLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.income_in_period),
                value = formatMoney(incomeInPeriod),
                accentColor = MaterialTheme.colorScheme.secondary,
                valueBlurred = areAmountsHidden,
                onClick = {
                    onViewAllTransactions(
                        null,
                        TransactionTypeFilter.INCOME,
                        currentRange.start.toEpochDay(),
                        currentRange.end.toEpochDay()
                    )
                }
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.expenses_in_period),
                value = formatMoney(expensesInPeriod),
                accentColor = MaterialTheme.colorScheme.tertiary,
                valueBlurred = areAmountsHidden,
                onClick = {
                    onViewAllTransactions(
                        null,
                        TransactionTypeFilter.EXPENSE,
                        currentRange.start.toEpochDay(),
                        currentRange.end.toEpochDay()
                    )
                }
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.net_in_period),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatMoney(netInPeriod),
                    modifier = Modifier.amountBlur(areAmountsHidden),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.expense_chart_title),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (selectedPeriod == AnalyticsPeriod.CUSTOM) {
                            customPeriodLabel
                        } else {
                            stringResource(selectedPeriod.labelRes)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                SpendingChart(
                    transactions = periodTransactions,
                    emptyLabel = stringResource(R.string.analytics_placeholder),
                    period = selectedPeriod,
                    range = currentRange,
                    areAmountsHidden = areAmountsHidden,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDateEpochDay = it.toEpochDay() }
                )
                if (expensesInPeriod > 0L && !areAmountsHidden) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.analytics_chart_tap_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.transactions_count, periodTransactions.size),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        AnalyticsCategorySpendingCard(
            categorySpending = categorySpending,
            categories = uiState.categories,
            areAmountsHidden = areAmountsHidden,
            onCategorySelected = {
                selectedCategoryId = it
                showCategoryDetails = true
            }
        )

        AnalyticsComparisonCard(
            period = selectedPeriod,
            comparisonMode = comparisonMode,
            onComparisonModeChanged = { comparisonMode = it },
            comparisonMonth = comparisonMonth,
            onChooseComparisonMonth = { showComparisonMonthPicker = true },
            currentRange = currentRange,
            comparisonRange = comparisonRange,
            currentIncome = incomeInPeriod,
            previousIncome = previousIncome,
            currentExpenses = expensesInPeriod,
            previousExpenses = previousExpenses,
            currentNet = netInPeriod,
            previousNet = previousNet,
            hasPreviousData = previousPeriodTransactions.isNotEmpty(),
            areAmountsHidden = areAmountsHidden
        )

        AnalyticsInsightsCard(
            range = currentRange,
            expenses = expensesInPeriod,
            spendingPoints = spendingPointsForPeriod(
                transactions = periodTransactions,
                period = selectedPeriod,
                range = currentRange
            ),
            topCategory = categorySpending.firstOrNull(),
            categories = uiState.categories,
            areAmountsHidden = areAmountsHidden,
            onHighestSpendingDaySelected = {
                selectedDateEpochDay = it.toEpochDay()
            }
        )

    }

    if (showCustomPeriodOptions) {
        AnalyticsCustomPeriodOptionsDialog(
            onDismiss = { showCustomPeriodOptions = false },
            onWholeMonthSelected = {
                showCustomPeriodOptions = false
                showCustomMonthPicker = true
            },
            onDateRangeSelected = {
                showCustomPeriodOptions = false
                showCustomRangePicker = true
            }
        )
    }

    if (showCustomMonthPicker) {
        MonthPickerDialog(
            titleRes = R.string.analytics_choose_analytics_month,
            initialMonth = customMonth,
            availableMonths = customMonths,
            onDismiss = { showCustomMonthPicker = false },
            onConfirm = { selectedMonth ->
                customMonthEpochDay = selectedMonth.withDayOfMonth(1).toEpochDay()
                customPeriodMode = AnalyticsCustomPeriodMode.WHOLE_MONTH
                showCustomMonthPicker = false
            }
        )
    }

    if (showCustomRangePicker) {
        DateRangePickerDialog(
            initialStartEpochDay = customStartEpochDay,
            initialEndEpochDay = customEndEpochDay,
            onDismiss = { showCustomRangePicker = false },
            onConfirm = { startEpochDay, endEpochDay ->
                customStartEpochDay = minOf(startEpochDay, endEpochDay)
                customEndEpochDay = maxOf(startEpochDay, endEpochDay)
                customPeriodMode = AnalyticsCustomPeriodMode.DATE_RANGE
                showCustomRangePicker = false
            }
        )
    }

    if (showComparisonMonthPicker) {
        MonthPickerDialog(
            titleRes = R.string.analytics_choose_comparison_month,
            initialMonth = comparisonMonth,
            availableMonths = comparisonMonths,
            onDismiss = { showComparisonMonthPicker = false },
            onConfirm = { selectedMonth ->
                comparisonMonthEpochDay = selectedMonth.withDayOfMonth(1).toEpochDay()
                comparisonMode = AnalyticsComparisonMode.SELECTED_MONTH
                showComparisonMonthPicker = false
            }
        )
    }

    if (showCategoryDetails) {
        AnalyticsCategoryDetailsSheet(
            categoryName = selectedCategory?.let { categoryLabel(it) }
                ?: stringResource(R.string.uncategorized),
            transactions = selectedCategoryTransactions,
            categories = uiState.categories,
            areAmountsHidden = areAmountsHidden,
            onViewAllTransactions = {
                showCategoryDetails = false
                onViewAllTransactions(
                    selectedCategoryId ?: UNCATEGORIZED_FILTER,
                    TransactionTypeFilter.EXPENSE,
                    currentRange.start.toEpochDay(),
                    currentRange.end.toEpochDay()
                )
            },
            onDismiss = { showCategoryDetails = false }
        )
    }

    selectedDate?.let { date ->
        AnalyticsDayDetailsSheet(
            date = date,
            transactions = selectedDayTransactions.orEmpty(),
            categories = uiState.categories,
            areAmountsHidden = areAmountsHidden,
            onViewAllTransactions = {
                selectedDateEpochDay = null
                onViewAllTransactions(
                    null,
                    TransactionTypeFilter.ALL,
                    date.toEpochDay(),
                    date.toEpochDay()
                )
            },
            onDismiss = { selectedDateEpochDay = null }
        )
    }
}
