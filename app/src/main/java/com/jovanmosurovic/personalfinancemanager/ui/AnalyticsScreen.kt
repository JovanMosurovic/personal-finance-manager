package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate

@Composable
internal fun AnalyticsScreen(
    uiState: FinanceUiState,
    areAmountsHidden: Boolean,
    onViewTransactions: (TransactionTypeFilter, Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by rememberSaveable { mutableStateOf(AnalyticsPeriod.LAST_7_DAYS) }
    val range = selectedPeriod.dateRange(LocalDate.now())
    val periodTransactions = uiState.transactions.filter {
        it.dateEpochDay in range.start.toEpochDay()..range.end.toEpochDay()
    }
    val income = periodTransactions
        .filter { it.type == TransactionType.INCOME.name }
        .sumOf { it.amountMinor }
    val expenses = periodTransactions
        .filter { it.type == TransactionType.EXPENSE.name }
        .sumOf { it.amountMinor }
    val categorySpending = categorySpendingForPeriod(uiState.transactions, selectedPeriod)
    val categoriesById = uiState.categories.associateBy { it.id }

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnalyticsPeriod.entries.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { selectedPeriod = period },
                    label = { Text(analyticsPeriodLabel(period)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
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
                value = formatMoney(income),
                accentColor = MaterialTheme.colorScheme.secondary,
                valueBlurred = areAmountsHidden,
                onClick = { onViewTransactions(TransactionTypeFilter.INCOME, null) }
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.expenses_in_period),
                value = formatMoney(expenses),
                accentColor = MaterialTheme.colorScheme.tertiary,
                valueBlurred = areAmountsHidden,
                onClick = { onViewTransactions(TransactionTypeFilter.EXPENSE, null) }
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.expense_chart_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                SpendingChart(
                    transactions = uiState.transactions,
                    period = selectedPeriod,
                    areAmountsHidden = areAmountsHidden,
                    emptyLabel = stringResource(R.string.analytics_no_data),
                    onDateSelected = { date ->
                        onViewTransactions(TransactionTypeFilter.ALL, date.toEpochDay())
                    }
                )
            }
        }

        Text(
            text = stringResource(R.string.spending_by_category),
            style = MaterialTheme.typography.titleLarge
        )
        if (categorySpending.isEmpty()) {
            Text(
                text = stringResource(R.string.analytics_no_data),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            categorySpending.forEach { spending ->
                CategorySpendingRow(
                    spending = spending,
                    category = spending.categoryId?.let(categoriesById::get),
                    areAmountsHidden = areAmountsHidden,
                    onClick = {
                        spending.categoryId?.let { id ->
                            onViewTransactions(TransactionTypeFilter.EXPENSE, id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CategorySpendingRow(
    spending: CategorySpending,
    category: CategoryEntity?,
    areAmountsHidden: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (category != null) {
                    categoryLabel(category)
                } else {
                    stringResource(R.string.uncategorized)
                },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.analytics_category_tap_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = formatMoney(spending.amountMinor),
            modifier = Modifier.amountBlur(areAmountsHidden),
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}
