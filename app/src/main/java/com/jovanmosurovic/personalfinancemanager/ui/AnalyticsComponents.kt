package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate
import kotlin.math.absoluteValue

@Composable
internal fun AnalyticsCategorySpendingCard(
    categorySpending: List<CategorySpending>,
    categories: List<CategoryEntity>,
    areAmountsHidden: Boolean,
    onCategorySelected: (Long?) -> Unit
) {
    val maxCategorySpend = categorySpending.maxOfOrNull { it.amountMinor } ?: 0L

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.spending_by_category),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(14.dp))
            if (categorySpending.isEmpty()) {
                Text(
                    text = stringResource(R.string.analytics_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (areAmountsHidden) {
                Text(
                    text = stringResource(R.string.amounts_hidden_chart),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                categorySpending.take(5).forEachIndexed { index, spending ->
                    val category = categories.firstOrNull { it.id == spending.categoryId }
                    val label = category?.let { categoryLabel(it) }
                        ?: stringResource(R.string.uncategorized)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                role = Role.Button,
                                onClick = { onCategorySelected(spending.categoryId) }
                            )
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.shapes.small
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(
                                            (spending.amountMinor.toFloat() / maxCategorySpend)
                                                .coerceIn(0.05f, 1f)
                                        )
                                        .height(8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.tertiary,
                                            MaterialTheme.shapes.small
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = formatMoney(spending.amountMinor),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (index < minOf(4, categorySpending.lastIndex)) {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
internal fun AnalyticsCustomPeriodOptionsDialog(
    onDismiss: () -> Unit,
    onWholeMonthSelected: () -> Unit,
    onDateRangeSelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.analytics_custom_period_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.analytics_custom_period_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onWholeMonthSelected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.analytics_whole_month))
                }
                OutlinedButton(
                    onClick = onDateRangeSelected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.analytics_custom_range))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
internal fun AnalyticsComparisonCard(
    period: AnalyticsPeriod,
    comparisonMode: AnalyticsComparisonMode,
    onComparisonModeChanged: (AnalyticsComparisonMode) -> Unit,
    comparisonMonth: LocalDate,
    onChooseComparisonMonth: () -> Unit,
    currentRange: AnalyticsDateRange,
    comparisonRange: AnalyticsDateRange,
    currentIncome: Long,
    previousIncome: Long,
    currentExpenses: Long,
    previousExpenses: Long,
    currentNet: Long,
    previousNet: Long,
    hasPreviousData: Boolean,
    areAmountsHidden: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.analytics_comparison_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.analytics_comparison_subtitle,
                    formatDateRange(currentRange),
                    formatDateRange(comparisonRange)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (period == AnalyticsPeriod.THIS_MONTH || period == AnalyticsPeriod.CUSTOM) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.analytics_comparison_basis),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    AnalyticsComparisonMode.entries.forEach { mode ->
                        item(key = mode.name) {
                            FilterChip(
                                selected = comparisonMode == mode,
                                onClick = { onComparisonModeChanged(mode) },
                                label = { Text(stringResource(mode.labelRes)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        .copy(alpha = 0.55f),
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
                if (comparisonMode == AnalyticsComparisonMode.PREVIOUS_MONTH) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.analytics_previous_month_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (comparisonMode == AnalyticsComparisonMode.SELECTED_MONTH) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onChooseComparisonMonth,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(formatMonthYear(comparisonMonth))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.analytics_selected_month_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            AnalyticsComparisonRow(
                label = stringResource(R.string.expenses_in_period),
                currentValue = currentExpenses,
                previousValue = previousExpenses,
                hasPreviousData = hasPreviousData,
                areAmountsHidden = areAmountsHidden,
                accentColor = MaterialTheme.colorScheme.tertiary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
            AnalyticsComparisonRow(
                label = stringResource(R.string.income_in_period),
                currentValue = currentIncome,
                previousValue = previousIncome,
                hasPreviousData = hasPreviousData,
                areAmountsHidden = areAmountsHidden,
                accentColor = MaterialTheme.colorScheme.secondary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
            AnalyticsComparisonRow(
                label = stringResource(R.string.net_in_period),
                currentValue = currentNet,
                previousValue = previousNet,
                hasPreviousData = hasPreviousData,
                areAmountsHidden = areAmountsHidden,
                accentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AnalyticsComparisonRow(
    label: String,
    currentValue: Long,
    previousValue: Long,
    hasPreviousData: Boolean,
    areAmountsHidden: Boolean,
    accentColor: Color
) {
    val difference = currentValue - previousValue
    val differenceColor = if (difference > 0L) {
        accentColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = accentColor
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ComparisonValue(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.analytics_current_period),
                value = currentValue,
                areAmountsHidden = areAmountsHidden,
                valueColor = MaterialTheme.colorScheme.onSurface
            )
            ComparisonValue(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.analytics_comparison_period),
                value = previousValue,
                areAmountsHidden = areAmountsHidden,
                valueColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (areAmountsHidden) {
            Text(
                text = stringResource(R.string.amounts_hidden_chart),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (!hasPreviousData) {
            Text(
                text = stringResource(R.string.analytics_change_no_baseline),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val absoluteDifference = difference.absoluteValue
            val percentage = percentageChange(currentValue, previousValue)
            val changeText = when {
                difference == 0L -> stringResource(R.string.analytics_change_same)
                previousValue == 0L -> stringResource(
                    if (difference > 0L) {
                        R.string.analytics_change_more_without_percentage
                    } else {
                        R.string.analytics_change_less_without_percentage
                    },
                    formatMoney(absoluteDifference)
                )
                difference > 0L -> stringResource(
                    R.string.analytics_change_more,
                    formatMoney(absoluteDifference),
                    percentage
                )
                else -> stringResource(
                    R.string.analytics_change_less,
                    formatMoney(absoluteDifference),
                    percentage
                )
            }
            Text(
                text = changeText,
                style = MaterialTheme.typography.labelSmall,
                color = differenceColor
            )
        }
    }
}

@Composable
private fun ComparisonValue(
    label: String,
    value: Long,
    areAmountsHidden: Boolean,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = formatMoney(value),
            modifier = Modifier.amountBlur(areAmountsHidden),
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun AnalyticsInsightsCard(
    range: AnalyticsDateRange,
    expenses: Long,
    spendingPoints: List<SpendingPoint>,
    topCategory: CategorySpending?,
    categories: List<CategoryEntity>,
    areAmountsHidden: Boolean,
    onHighestSpendingDaySelected: (LocalDate) -> Unit
) {
    val dailyAverage = if (range.dayCount > 0) {
        expenses / range.dayCount.toLong()
    } else {
        0L
    }
    val highestSpendingDay = spendingPoints
        .filter { it.amountMinor > 0L }
        .maxByOrNull { it.amountMinor }
    val topCategoryLabel = topCategory?.let { spending ->
        categories.firstOrNull { it.id == spending.categoryId }?.let { categoryLabel(it) }
            ?: stringResource(R.string.uncategorized)
    } ?: stringResource(R.string.analytics_no_data)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.analytics_insights_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(14.dp))
            AnalyticsInsightRow(
                icon = Icons.Outlined.ArrowDownward,
                label = stringResource(R.string.analytics_average_daily_spending),
                value = formatMoney(dailyAverage),
                valueBlurred = areAmountsHidden
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            AnalyticsInsightRow(
                icon = Icons.Outlined.DateRange,
                label = stringResource(R.string.analytics_highest_spending_day),
                value = highestSpendingDay?.let {
                    stringResource(
                        R.string.analytics_day_and_amount,
                        formatDate(it.date.toEpochDay()),
                        formatMoney(it.amountMinor)
                    )
                } ?: stringResource(R.string.analytics_no_data),
                valueBlurred = areAmountsHidden && highestSpendingDay != null,
                onClick = highestSpendingDay?.date?.let { date ->
                    { onHighestSpendingDaySelected(date) }
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            AnalyticsInsightRow(
                icon = Icons.Outlined.Category,
                label = stringResource(R.string.analytics_top_category),
                value = topCategoryLabel,
                valueBlurred = false
            )
        }
    }
}

@Composable
private fun AnalyticsInsightRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueBlurred: Boolean,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = if (onClick == null) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            modifier = Modifier.amountBlur(valueBlurred),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun AnalyticsCategoryDetailsSheet(
    categoryName: String,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    areAmountsHidden: Boolean,
    onViewAllTransactions: () -> Unit,
    onDismiss: () -> Unit
) {
    val expenseTotal = transactions.sumOf { it.amountMinor }
    val summary = if (areAmountsHidden) {
        stringResource(
            R.string.analytics_category_total_hidden,
            transactions.size
        )
    } else {
        stringResource(
            R.string.analytics_category_total,
            transactions.size,
            formatMoney(expenseTotal)
        )
    }

    AnalyticsTransactionsSheet(
        title = stringResource(
            R.string.analytics_category_transactions_title,
            categoryName
        ),
        summary = summary,
        emptyMessage = stringResource(R.string.analytics_no_transactions_category),
        icon = Icons.Outlined.Category,
        transactions = transactions,
        categories = categories,
        areAmountsHidden = areAmountsHidden,
        onViewAllTransactions = onViewAllTransactions,
        onDismiss = onDismiss
    )
}

@Composable
internal fun AnalyticsDayDetailsSheet(
    date: LocalDate,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    areAmountsHidden: Boolean,
    onViewAllTransactions: () -> Unit,
    onDismiss: () -> Unit
) {
    val expenseTotal = transactions
        .filter { it.type == TransactionType.EXPENSE.name }
        .sumOf { it.amountMinor }

    val summary = if (areAmountsHidden) {
        stringResource(
            R.string.analytics_day_total_hidden,
            transactions.size
        )
    } else {
        stringResource(
            R.string.analytics_day_total,
            transactions.size,
            formatMoney(expenseTotal)
        )
    }

    AnalyticsTransactionsSheet(
        title = stringResource(
            R.string.analytics_day_transactions_title,
            formatDate(date.toEpochDay())
        ),
        summary = summary,
        emptyMessage = stringResource(R.string.analytics_no_transactions_day),
        icon = Icons.Outlined.DateRange,
        transactions = transactions,
        categories = categories,
        areAmountsHidden = areAmountsHidden,
        onViewAllTransactions = onViewAllTransactions,
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsTransactionsSheet(
    title: String,
    summary: String,
    emptyMessage: String,
    icon: ImageVector,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    areAmountsHidden: Boolean,
    onViewAllTransactions: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (transactions.isEmpty()) {
                Text(
                    text = emptyMessage,
                    modifier = Modifier.padding(vertical = 20.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(
                        items = transactions,
                        key = { it.id }
                    ) { transaction ->
                        CompactTransactionRow(
                            transaction = transaction,
                            category = categories.firstOrNull {
                                it.id == transaction.categoryId
                            },
                            areAmountsHidden = areAmountsHidden
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onViewAllTransactions,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.view_all_transactions))
            }
        }
    }
}
