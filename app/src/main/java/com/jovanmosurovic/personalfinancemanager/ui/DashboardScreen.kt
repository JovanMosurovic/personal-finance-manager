package com.jovanmosurovic.personalfinancemanager.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity

@Composable
internal fun DashboardScreen(
    uiState: FinanceUiState,
    areAmountsHidden: Boolean,
    onAmountsVisibilityChanged: (Boolean) -> Unit,
    onViewTransactions: (TransactionTypeFilter, Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dashboard_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineLarge
            )
            IconButton(onClick = { onAmountsVisibilityChanged(!areAmountsHidden) }) {
                Icon(
                    imageVector = if (areAmountsHidden) {
                        Icons.Outlined.VisibilityOff
                    } else {
                        Icons.Outlined.Visibility
                    },
                    contentDescription = stringResource(
                        if (areAmountsHidden) R.string.show_amounts else R.string.hide_amounts
                    )
                )
            }
        }
        Text(
            text = stringResource(R.string.dashboard_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.balance_total),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatMoney(uiState.totalBalanceMinor),
                    modifier = Modifier.amountBlur(areAmountsHidden),
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.balance_currency_label),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.income_this_month),
                value = formatMoney(uiState.incomeThisMonthMinor),
                accentColor = MaterialTheme.colorScheme.secondary,
                valueBlurred = areAmountsHidden,
                onClick = { onViewTransactions(TransactionTypeFilter.INCOME, null) }
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.expenses_this_month),
                value = formatMoney(uiState.expensesThisMonthMinor),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.expense_chart_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                SpendingChart(
                    transactions = uiState.transactions,
                    period = AnalyticsPeriod.LAST_7_DAYS,
                    areAmountsHidden = areAmountsHidden,
                    emptyLabel = stringResource(R.string.expense_chart_placeholder),
                    onDateSelected = { date ->
                        onViewTransactions(TransactionTypeFilter.ALL, date.toEpochDay())
                    }
                )
            }
        }

        RecentActivityCard(
            transactions = uiState.transactions,
            categories = uiState.categories,
            areAmountsHidden = areAmountsHidden,
            onViewAll = { onViewTransactions(TransactionTypeFilter.ALL, null) }
        )
    }
}

@Composable
private fun RecentActivityCard(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    areAmountsHidden: Boolean,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.dashboard_recent_transactions),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onViewAll) {
                    Text(stringResource(R.string.view_all))
                }
            }
            if (transactions.isEmpty()) {
                Text(
                    text = stringResource(R.string.transactions_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                transactions.take(3).forEachIndexed { index, transaction ->
                    CompactTransactionRow(
                        transaction = transaction,
                        category = categories.firstOrNull { it.id == transaction.categoryId },
                        areAmountsHidden = areAmountsHidden
                    )
                    if (index < minOf(2, transactions.lastIndex)) {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}
