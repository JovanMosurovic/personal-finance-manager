package com.jovanmosurovic.personalfinancemanager.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jovanmosurovic.personalfinancemanager.FinanceApplication
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private enum class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    DASHBOARD("dashboard", R.string.nav_dashboard, Icons.Outlined.Home, Icons.Filled.Home),
    TRANSACTIONS(
        "transactions",
        R.string.nav_transactions,
        Icons.AutoMirrored.Outlined.ReceiptLong,
        Icons.AutoMirrored.Filled.ReceiptLong
    ),
    ANALYTICS("analytics", R.string.nav_analytics, Icons.Outlined.Analytics, Icons.Filled.Analytics),
    CATEGORIES("categories", R.string.nav_categories, Icons.Outlined.Category, Icons.Filled.Category),
    SETTINGS("settings", R.string.nav_settings, Icons.Outlined.Settings, Icons.Filled.Settings)
}

private enum class AppLanguage(
    val languageTag: String,
    val labelRes: Int
) {
    SERBIAN_LATIN("sr-Latn", R.string.language_serbian_latin),
    ENGLISH("en", R.string.language_english)
}

private enum class AnalyticsPeriod(
    val labelRes: Int
) {
    LAST_7_DAYS(R.string.analytics_period_7_days),
    LAST_30_DAYS(R.string.analytics_period_30_days),
    THIS_MONTH(R.string.analytics_period_this_month)
}

@Composable
fun PersonalFinanceApp() {
    val context = LocalContext.current
    val application = context.applicationContext as FinanceApplication
    val financeViewModel: FinanceViewModel = viewModel(
        factory = FinanceViewModel.Factory(application.repository)
    )
    val uiState by financeViewModel.uiState.collectAsStateWithLifecycle()

    var selectedRoute by rememberSaveable {
        mutableStateOf(TopLevelDestination.DASHBOARD.route)
    }
    var showAddTransaction by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!showAddTransaction) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                        )
                    ) {
                        NavigationBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            windowInsets = WindowInsets(0, 0, 0, 0)
                        ) {
                            TopLevelDestination.entries.forEach { destination ->
                                NavigationBarItem(
                                    selected = selectedRoute == destination.route,
                                    onClick = { selectedRoute = destination.route },
                                    icon = {
                                        Icon(
                                            imageVector = if (selectedRoute == destination.route) {
                                                destination.selectedIcon
                                            } else {
                                                destination.icon
                                            },
                                            contentDescription = null
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(destination.labelRes),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                lineHeight = 14.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (!uiState.isReady) {
            LoadingScreen(modifier = Modifier.padding(innerPadding))
        } else if (showAddTransaction) {
            AddTransactionScreen(
                modifier = Modifier.padding(innerPadding),
                onCancel = { showAddTransaction = false },
                onSave = { type, amountMinor, merchant, note, dateEpochDay ->
                    financeViewModel.addTransaction(
                        type = type,
                        amountMinor = amountMinor,
                        merchant = merchant,
                        note = note,
                        dateEpochDay = dateEpochDay
                    )
                    showAddTransaction = false
                }
            )
        } else {
            when (selectedRoute) {
                TopLevelDestination.TRANSACTIONS.route -> TransactionsScreen(
                    uiState = uiState,
                    onAddTransaction = { showAddTransaction = true },
                    onAssignCategory = financeViewModel::assignCategory,
                    onUpdateTransaction = financeViewModel::updateTransaction,
                    onDeleteTransaction = financeViewModel::deleteTransaction,
                    modifier = Modifier.padding(innerPadding)
                )

                TopLevelDestination.ANALYTICS.route -> AnalyticsScreen(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )

                TopLevelDestination.CATEGORIES.route -> CategoriesScreen(
                    uiState = uiState,
                    onAddKeyword = financeViewModel::addKeyword,
                    onDeleteKeyword = financeViewModel::deleteKeyword,
                    modifier = Modifier.padding(innerPadding)
                )

                TopLevelDestination.SETTINGS.route -> SettingsScreen(
                    modifier = Modifier.padding(innerPadding)
                )

                else -> DashboardScreen(
                    uiState = uiState,
                    onViewTransactions = {
                        selectedRoute = TopLevelDestination.TRANSACTIONS.route
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    uiState: FinanceUiState,
    onViewTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = stringResource(R.string.dashboard_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.balance_total),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = formatMoney(uiState.totalBalanceMinor),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.balance_currency_label),
                    style = MaterialTheme.typography.bodyMedium,
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
                accentColor = MaterialTheme.colorScheme.secondary
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.expenses_this_month),
                value = formatMoney(uiState.expensesThisMonthMinor),
                accentColor = MaterialTheme.colorScheme.tertiary
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
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.chart_last_7_days),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                SpendingChart(
                    transactions = uiState.transactions,
                    emptyLabel = stringResource(R.string.expense_chart_placeholder)
                )
            }
        }

        RecentActivityCard(
            transactions = uiState.transactions,
            categories = uiState.categories,
            onViewAll = onViewTransactions
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.10f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (accentColor == MaterialTheme.colorScheme.secondary) {
                        Icons.Outlined.ArrowUpward
                    } else {
                        Icons.Outlined.ArrowDownward
                    },
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private data class SpendingPoint(
    val date: LocalDate,
    val amountMinor: Long
)

@Composable
private fun SpendingChart(
    transactions: List<TransactionEntity>,
    emptyLabel: String,
    period: AnalyticsPeriod = AnalyticsPeriod.LAST_7_DAYS,
    modifier: Modifier = Modifier
) {
    val points = spendingPointsForPeriod(transactions, period)
    val maxAmount = points.maxOfOrNull { it.amountMinor } ?: 0L
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.tertiary

    if (maxAmount == 0L) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(128.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
        ) {
            val bottom = size.height - 8.dp.toPx()
            val top = 8.dp.toPx()
            val chartHeight = bottom - top
            val gap = if (points.size > 14) 3.dp.toPx() else 7.dp.toPx()
            val barWidth = (size.width - gap * (points.size - 1)) / points.size

            listOf(0f, 0.5f, 1f).forEach { progress ->
                val y = bottom - (chartHeight * progress)
                drawLine(
                    color = outlineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            points.forEachIndexed { index, point ->
                val barHeight = ((point.amountMinor.toFloat() / maxAmount) * chartHeight)
                    .coerceAtLeast(4.dp.toPx())
                val x = index * (barWidth + gap)
                drawRoundRect(
                    color = if (index == points.lastIndex) {
                        primaryColor
                    } else {
                        expenseColor
                    },
                    topLeft = Offset(x, bottom - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            val labelStep = when {
                points.size <= 7 -> 1
                points.size <= 14 -> 2
                else -> 5
            }
            points.forEachIndexed { index, point ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (index == 0 || index == points.lastIndex || index % labelStep == 0) {
                        Text(
                            text = chartDayLabel(point.date, points.size > 7),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun spendingPointsForPeriod(
    transactions: List<TransactionEntity>,
    period: AnalyticsPeriod
): List<SpendingPoint> {
    val today = LocalDate.now()
    val startDate = period.startDate(today)
    val dayCount = ChronoUnit.DAYS.between(startDate, today).toInt() + 1

    return (0 until dayCount).map { dayOffset ->
        val date = startDate.plusDays(dayOffset.toLong())
        SpendingPoint(
            date = date,
            amountMinor = transactions
                .asSequence()
                .filter { transaction ->
                    transaction.type == TransactionType.EXPENSE.name &&
                        transaction.dateEpochDay == date.toEpochDay()
                }
                .sumOf { it.amountMinor }
        )
    }
}

private fun chartDayLabel(date: LocalDate, useDate: Boolean): String = if (useDate) {
    DateTimeFormatter.ofPattern("d.M.", Locale.getDefault()).format(date)
} else {
    DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        .format(date)
        .replace(".", "")
        .take(3)
        .uppercase(Locale.getDefault())
}

private fun AnalyticsPeriod.startDate(today: LocalDate): LocalDate = when (this) {
    AnalyticsPeriod.LAST_7_DAYS -> today.minusDays(6)
    AnalyticsPeriod.LAST_30_DAYS -> today.minusDays(29)
    AnalyticsPeriod.THIS_MONTH -> today.withDayOfMonth(1)
}

private fun transactionsInPeriod(
    transactions: List<TransactionEntity>,
    period: AnalyticsPeriod,
    today: LocalDate = LocalDate.now()
): List<TransactionEntity> {
    val startEpochDay = period.startDate(today).toEpochDay()
    val endEpochDay = today.toEpochDay()
    return transactions.filter {
        it.dateEpochDay in startEpochDay..endEpochDay
    }
}

@Composable
private fun RecentActivityCard(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                transactions.take(3).forEachIndexed { index, transaction ->
                    CompactTransactionRow(
                        transaction = transaction,
                        category = categories.firstOrNull { it.id == transaction.categoryId }
                    )
                    if (index < minOf(2, transactions.lastIndex)) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactTransactionRow(
    transaction: TransactionEntity,
    category: CategoryEntity?
) {
    val isIncome = transaction.type == TransactionType.INCOME.name
    val accentColor = if (isIncome) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TransactionBadge(transaction = transaction, color = accentColor)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.merchant,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Text(
                text = category?.let { categoryLabel(it) }
                    ?: stringResource(R.string.no_category_assigned),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Text(
            text = formatMoney(if (isIncome) transaction.amountMinor else -transaction.amountMinor),
            style = MaterialTheme.typography.titleMedium,
            color = accentColor,
            maxLines = 1
        )
    }
}

@Composable
private fun TransactionBadge(
    transaction: TransactionEntity,
    color: Color
) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.14f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (transaction.type == TransactionType.INCOME.name) {
                    Icons.Outlined.ArrowUpward
                } else {
                    Icons.Outlined.ArrowDownward
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TransactionsScreen(
    uiState: FinanceUiState,
    onAddTransaction: () -> Unit,
    onAssignCategory: (Long, Long, Boolean) -> Unit,
    onUpdateTransaction: (Long, TransactionType, Long, String, String, Long) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOnlyUncategorized by rememberSaveable { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionForCategory by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionBeingEdited by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionPendingDeletion by remember { mutableStateOf<TransactionEntity?>(null) }
    val uncategorizedTransactions = uiState.transactions.filter { it.categoryId == null }
    val visibleTransactions = if (showOnlyUncategorized) {
        uncategorizedTransactions
    } else {
        uiState.transactions
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null
                    )
                },
                text = { Text(stringResource(R.string.add_transaction)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = stringResource(R.string.transactions_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = stringResource(R.string.transactions_count, uiState.transactions.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            FilterChip(
                selected = showOnlyUncategorized,
                onClick = { showOnlyUncategorized = !showOnlyUncategorized },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        stringResource(
                            R.string.uncategorized_count,
                            uncategorizedTransactions.size
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (visibleTransactions.isEmpty() && showOnlyUncategorized) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = stringResource(R.string.no_uncategorized_transactions),
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (visibleTransactions.isEmpty()) {
                EmptyTransactionsContent()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visibleTransactions, key = { it.id }) { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            category = uiState.categories.firstOrNull {
                                it.id == transaction.categoryId
                            },
                            onClick = { selectedTransaction = transaction }
                        )
                    }
                }
            }
        }
    }

    selectedTransaction?.let { transaction ->
        TransactionDetailsDialog(
            transaction = transaction,
            category = uiState.categories.firstOrNull { it.id == transaction.categoryId },
            onDismiss = { selectedTransaction = null },
            onEdit = {
                transactionBeingEdited = transaction
                selectedTransaction = null
            },
            onAssignCategory = {
                transactionForCategory = transaction
                selectedTransaction = null
            },
            onDelete = {
                transactionPendingDeletion = transaction
                selectedTransaction = null
            }
        )
    }

    transactionForCategory?.let { transaction ->
        CategoryAssignmentDialog(
            transaction = transaction,
            categories = uiState.categories,
            onDismiss = { transactionForCategory = null },
            onConfirm = { categoryId, saveMerchantAsKeyword ->
                onAssignCategory(transaction.id, categoryId, saveMerchantAsKeyword)
                transactionForCategory = null
            }
        )
    }

    transactionBeingEdited?.let { transaction ->
        EditTransactionDialog(
            transaction = transaction,
            onDismiss = { transactionBeingEdited = null },
            onSave = { type, amountMinor, merchant, note, dateEpochDay ->
                onUpdateTransaction(
                    transaction.id,
                    type,
                    amountMinor,
                    merchant,
                    note,
                    dateEpochDay
                )
                transactionBeingEdited = null
            }
        )
    }

    transactionPendingDeletion?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionPendingDeletion = null },
            title = {
                Text(stringResource(R.string.delete_transaction_title))
            },
            text = {
                Text(
                    stringResource(
                        R.string.delete_transaction_confirmation,
                        transaction.merchant
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(transaction.id)
                        transactionPendingDeletion = null
                    }
                ) {
                    Text(stringResource(R.string.delete_transaction))
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionPendingDeletion = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun TransactionDetailsDialog(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onAssignCategory: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME.name
    val amountColor = if (isIncome) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.transaction_details))
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TransactionBadge(transaction = transaction, color = amountColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = transaction.merchant,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatMoney(if (isIncome) transaction.amountMinor else -transaction.amountMinor),
                                style = MaterialTheme.typography.titleMedium,
                                color = amountColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                HorizontalDivider()
                TransactionDetailRow(
                    label = stringResource(R.string.date_label),
                    value = formatDate(transaction.dateEpochDay)
                )
                TransactionDetailRow(
                    label = stringResource(R.string.category),
                    value = category?.let { categoryLabel(it) }
                        ?: stringResource(R.string.no_category_assigned)
                )
                if (transaction.note.isNotBlank()) {
                    TransactionDetailRow(
                        label = stringResource(R.string.note),
                        value = transaction.note
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(top = 2.dp))
                OutlinedButton(
                    onClick = onAssignCategory,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Category,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.change_category),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.delete),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onEdit) {
                Text(stringResource(R.string.edit_transaction))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun TransactionDetailRow(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun EditTransactionDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onSave: (TransactionType, Long, String, String, Long) -> Unit
) {
    var selectedTypeName by rememberSaveable(transaction.id) {
        mutableStateOf(transaction.type)
    }
    var amount by rememberSaveable(transaction.id) {
        mutableStateOf(formatEditableAmount(transaction.amountMinor))
    }
    var merchant by rememberSaveable(transaction.id) {
        mutableStateOf(transaction.merchant)
    }
    var note by rememberSaveable(transaction.id) {
        mutableStateOf(transaction.note)
    }
    var selectedDateEpochDay by rememberSaveable(transaction.id) {
        mutableStateOf(transaction.dateEpochDay)
    }
    var errorMessageRes by rememberSaveable(transaction.id) {
        mutableStateOf<Int?>(null)
    }

    val selectedType = TransactionType.valueOf(selectedTypeName)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.edit_transaction))
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TransactionTypeSelector(
                    selectedType = selectedType,
                    onTypeSelected = { selectedTypeName = it.name }
                )

                TextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        errorMessageRes = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.amount)) },
                    placeholder = { Text(stringResource(R.string.amount_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = MaterialTheme.shapes.medium,
                    colors = financeTextFieldColors()
                )

                TextField(
                    value = merchant,
                    onValueChange = {
                        merchant = it
                        errorMessageRes = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.merchant)) },
                    placeholder = { Text(stringResource(R.string.merchant_hint)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = financeTextFieldColors()
                )

                TransactionDateSelector(
                    dateEpochDay = selectedDateEpochDay,
                    onDateSelected = { selectedDateEpochDay = it }
                )

                TextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.note)) },
                    supportingText = { Text(stringResource(R.string.note_optional)) },
                    minLines = 2,
                    shape = MaterialTheme.shapes.medium,
                    colors = financeTextFieldColors()
                )

                errorMessageRes?.let { messageRes ->
                    Text(
                        text = stringResource(messageRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountMinor = parseAmountToMinor(amount)
                    errorMessageRes = when {
                        amountMinor == null || amountMinor <= 0L -> R.string.invalid_amount
                        merchant.isBlank() -> R.string.merchant_required
                        else -> null
                    }
                    if (errorMessageRes == null && amountMinor != null) {
                        onSave(
                            selectedType,
                            amountMinor,
                            merchant,
                            note,
                            selectedDateEpochDay
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun EmptyTransactionsContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.transactions_empty),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.transactions_empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    onClick: () -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME.name
    val amount = formatMoney(if (isIncome) transaction.amountMinor else -transaction.amountMinor)
    val amountColor = if (isIncome) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransactionBadge(transaction = transaction, color = amountColor)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchant,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = category?.let { categoryLabel(it) }
                        ?: stringResource(R.string.no_category_assigned),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatDate(transaction.dateEpochDay),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                color = amountColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CategoryAssignmentDialog(
    transaction: TransactionEntity,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Boolean) -> Unit
) {
    var selectedCategoryId by rememberSaveable(transaction.id) {
        mutableStateOf(transaction.categoryId ?: 0L)
    }
    var saveMerchantAsKeyword by rememberSaveable(transaction.id) {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.assign_category))
        },
        text = {
            val isIncome = transaction.type == TransactionType.INCOME.name
            val amountColor = if (isIncome) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.tertiary
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TransactionBadge(transaction = transaction, color = amountColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = transaction.merchant,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatMoney(
                                    if (isIncome) transaction.amountMinor else -transaction.amountMinor
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = amountColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.select_category),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        contentPadding = PaddingValues(4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(categories, key = { it.id }) { category ->
                            val parent = categories.firstOrNull { it.id == category.parentId }
                            val isSelected = selectedCategoryId == category.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCategoryId = category.id },
                                shape = MaterialTheme.shapes.small,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                                } else {
                                    Color.Transparent
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedCategoryId = category.id }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = categoryLabel(category),
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (parent != null) {
                                            Text(
                                                text = categoryLabel(parent),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            saveMerchantAsKeyword = !saveMerchantAsKeyword
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.remember_keyword),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = saveMerchantAsKeyword,
                        onCheckedChange = { saveMerchantAsKeyword = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            checkedBorderColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedCategoryId, saveMerchantAsKeyword)
                },
                enabled = selectedCategoryId != 0L
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun CategoriesScreen(
    uiState: FinanceUiState,
    onAddKeyword: (Long, String) -> Unit,
    onDeleteKeyword: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryForKeyword by remember { mutableStateOf<CategoryEntity?>(null) }
    var keywordPendingDeletion by remember { mutableStateOf<KeywordRuleEntity?>(null) }
    val topLevelCategories = uiState.categories.filter { it.parentId == null }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = stringResource(R.string.categories_title),
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = stringResource(R.string.categories_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        topLevelCategories.forEach { topLevelCategory ->
            val children = uiState.categories.filter {
                it.parentId == topLevelCategory.id
            }
            CategoryGroup(
                topLevelCategory = topLevelCategory,
                children = children,
                keywordRules = uiState.keywordRules,
                onAddKeyword = { categoryForKeyword = it },
                onDeleteKeyword = { keywordPendingDeletion = it }
            )
        }
    }

    categoryForKeyword?.let { category ->
        AddKeywordDialog(
            category = category,
            onDismiss = { categoryForKeyword = null },
            onConfirm = { keyword ->
                onAddKeyword(category.id, keyword)
                categoryForKeyword = null
            }
        )
    }

    keywordPendingDeletion?.let { rule ->
        AlertDialog(
            onDismissRequest = { keywordPendingDeletion = null },
            title = {
                Text(stringResource(R.string.delete_keyword_title))
            },
            text = {
                Text(
                    stringResource(
                        R.string.delete_keyword_confirmation,
                        rule.keyword
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteKeyword(rule.id)
                        keywordPendingDeletion = null
                    }
                ) {
                    Text(stringResource(R.string.delete_keyword))
                }
            },
            dismissButton = {
                TextButton(onClick = { keywordPendingDeletion = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun CategoryGroup(
    topLevelCategory: CategoryEntity,
    children: List<CategoryEntity>,
    keywordRules: List<KeywordRuleEntity>,
    onAddKeyword: (CategoryEntity) -> Unit,
    onDeleteKeyword: (KeywordRuleEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(category = topLevelCategory)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = categoryLabel(topLevelCategory),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(onClick = { onAddKeyword(topLevelCategory) }) {
                    Text(stringResource(R.string.add_keyword))
                }
            }
            val topLevelKeywords = keywordRules.filter {
                it.categoryId == topLevelCategory.id
            }
            if (topLevelKeywords.isNotEmpty()) {
                KeywordChips(topLevelKeywords, onDeleteKeyword)
            }
            children.forEach { category ->
                CategoryKeywordRow(
                    category = category,
                    keywords = keywordRules.filter { it.categoryId == category.id },
                    onAddKeyword = { onAddKeyword(category) },
                    onDeleteKeyword = onDeleteKeyword
                )
            }
        }
    }
}

@Composable
private fun CategoryBadge(category: CategoryEntity) {
    Surface(
        modifier = Modifier.size(38.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = categoryLabel(category).take(1).uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun CategoryKeywordRow(
    category: CategoryEntity,
    keywords: List<KeywordRuleEntity>,
    onAddKeyword: () -> Unit,
    onDeleteKeyword: (KeywordRuleEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryLabel(category),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onAddKeyword) {
                Text(stringResource(R.string.add_keyword))
            }
        }
        if (keywords.isEmpty()) {
            Text(
                text = stringResource(R.string.no_keywords),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            KeywordChips(keywords, onDeleteKeyword)
        }
    }
}

@Composable
private fun KeywordChips(
    keywords: List<KeywordRuleEntity>,
    onDeleteKeyword: (KeywordRuleEntity) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(keywords, key = { it.id }) { rule ->
            AssistChip(
                onClick = { onDeleteKeyword(rule) },
                label = {
                    Text(
                        text = rule.keyword,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.delete_keyword),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun AddKeywordDialog(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var keyword by rememberSaveable(category.id) { mutableStateOf("") }
    val categoryName = categoryLabel(category)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.keyword_dialog_title, categoryName))
        },
        text = {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                label = { Text(stringResource(R.string.add_keyword)) },
                placeholder = { Text(stringResource(R.string.keyword_hint)) },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(keyword) },
                enabled = keyword.trim().isNotEmpty()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun AnalyticsScreen(
    uiState: FinanceUiState,
    modifier: Modifier = Modifier
) {
    var selectedPeriodName by rememberSaveable {
        mutableStateOf(AnalyticsPeriod.LAST_7_DAYS.name)
    }
    val selectedPeriod = AnalyticsPeriod.valueOf(selectedPeriodName)
    val periodTransactions = transactionsInPeriod(uiState.transactions, selectedPeriod)
    val incomeInPeriod = periodTransactions
        .filter { it.type == TransactionType.INCOME.name }
        .sumOf { it.amountMinor }
    val expensesInPeriod = periodTransactions
        .filter { it.type == TransactionType.EXPENSE.name }
        .sumOf { it.amountMinor }
    val netInPeriod = incomeInPeriod - expensesInPeriod
    val categorySpending = categorySpendingForPeriod(periodTransactions)
    val maxCategorySpend = categorySpending.maxOfOrNull { it.amountMinor } ?: 0L

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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            AnalyticsPeriod.entries.forEach { period ->
                item(key = period.name) {
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriodName = period.name },
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.income_in_period),
                value = formatMoney(incomeInPeriod),
                accentColor = MaterialTheme.colorScheme.secondary
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.expenses_in_period),
                value = formatMoney(expensesInPeriod),
                accentColor = MaterialTheme.colorScheme.tertiary
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
                        text = stringResource(selectedPeriod.labelRes),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                SpendingChart(
                    transactions = periodTransactions,
                    emptyLabel = stringResource(R.string.analytics_placeholder),
                    period = selectedPeriod
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.transactions_count, periodTransactions.size),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

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
                } else {
                    categorySpending.take(5).forEachIndexed { index, spending ->
                        val category = uiState.categories.firstOrNull {
                            it.id == spending.categoryId
                        }
                        val label = category?.let { categoryLabel(it) }
                            ?: stringResource(R.string.uncategorized)
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
                        if (index < minOf(4, categorySpending.lastIndex)) {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }
        }
    }
}

private data class CategorySpending(
    val categoryId: Long?,
    val amountMinor: Long
)

private fun categorySpendingForPeriod(
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

@Composable
private fun AddTransactionScreen(
    onCancel: () -> Unit,
    onSave: (TransactionType, Long, String, String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTypeName by rememberSaveable { mutableStateOf(TransactionType.EXPENSE.name) }
    var amount by rememberSaveable { mutableStateOf("") }
    var merchant by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var selectedDateEpochDay by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var errorMessageRes by rememberSaveable { mutableStateOf<Int?>(null) }

    val selectedType = TransactionType.valueOf(selectedTypeName)

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
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.cancel)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.add_transaction),
                style = MaterialTheme.typography.headlineLarge
            )
        }

        TransactionTypeSelector(
            selectedType = selectedType,
            onTypeSelected = { selectedTypeName = it.name }
        )

        TextField(
            value = amount,
            onValueChange = {
                amount = it
                errorMessageRes = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.amount)) },
            placeholder = { Text(stringResource(R.string.amount_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = MaterialTheme.shapes.medium,
            colors = financeTextFieldColors()
        )

        TextField(
            value = merchant,
            onValueChange = {
                merchant = it
                errorMessageRes = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.merchant)) },
            placeholder = { Text(stringResource(R.string.merchant_hint)) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = financeTextFieldColors()
        )

        TransactionDateSelector(
            dateEpochDay = selectedDateEpochDay,
            onDateSelected = { selectedDateEpochDay = it }
        )

        TextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.note)) },
            supportingText = { Text(stringResource(R.string.note_optional)) },
            minLines = 2,
            shape = MaterialTheme.shapes.medium,
            colors = financeTextFieldColors()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.automatic_category_info),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        errorMessageRes?.let { messageRes ->
            Text(
                text = stringResource(messageRes),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = {
                val amountMinor = parseAmountToMinor(amount)
                errorMessageRes = when {
                    amountMinor == null || amountMinor <= 0L -> R.string.invalid_amount
                    merchant.isBlank() -> R.string.merchant_required
                    else -> null
                }
                if (errorMessageRes == null && amountMinor != null) {
                    onSave(selectedType, amountMinor, merchant, note, selectedDateEpochDay)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDateSelector(
    dateEpochDay: Long,
    onDateSelected: (Long) -> Unit
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.date_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(dateEpochDay),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateEpochDay.toDatePickerMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.datePickerMillisToEpochDay()
                            ?.let(onDateSelected)
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .height(48.dp)
        ) {
            val itemWidth = maxWidth / 2
            val indicatorOffset by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (selectedType == TransactionType.EXPENSE) {
                    0.dp
                } else {
                    itemWidth
                },
                animationSpec = tween(
                    durationMillis = 260,
                    easing = FastOutSlowInEasing
                ),
                label = "transaction type indicator"
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    )
            )

            Row(modifier = Modifier.fillMaxSize()) {
                TransactionTypeOption(
                    label = stringResource(R.string.add_expense),
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { onTypeSelected(TransactionType.EXPENSE) },
                    modifier = Modifier.weight(1f)
                )
                TransactionTypeOption(
                    label = stringResource(R.string.add_income),
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { onTypeSelected(TransactionType.INCOME) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TransactionTypeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 180),
        label = "transaction type content color"
    )

    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .semantics { role = Role.RadioButton },
        shape = MaterialTheme.shapes.small,
        color = Color.Transparent,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun financeTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    errorIndicatorColor = MaterialTheme.colorScheme.error
)

@Composable
private fun SettingsScreen(modifier: Modifier = Modifier) {
    val selectedLanguage = remember {
        currentAppLanguage()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.language_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.language_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                AppLanguage.entries.forEach { language ->
                    LanguageOption(
                        language = language,
                        selected = language == selectedLanguage,
                        onClick = {
                            if (language != selectedLanguage) {
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags(language.languageTag)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageOption(
    language: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                } else {
                    Color.Transparent
                },
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics { role = Role.RadioButton },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(language.labelRes))
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun categoryLabel(category: CategoryEntity): String =
    stringResource(categoryLabelRes(category.nameKey))

private fun categoryLabelRes(nameKey: String): Int = when (nameKey) {
    "category_food" -> R.string.category_food
    "category_groceries" -> R.string.category_groceries
    "category_fast_food" -> R.string.category_fast_food
    "category_delivery" -> R.string.category_delivery
    "category_kiosk" -> R.string.category_kiosk
    "category_utilities" -> R.string.category_utilities
    "category_phone_internet" -> R.string.category_phone_internet
    "category_infostan" -> R.string.category_infostan
    "category_electricity" -> R.string.category_electricity
    "category_car" -> R.string.category_car
    "category_fuel" -> R.string.category_fuel
    "category_parking" -> R.string.category_parking
    "category_registration" -> R.string.category_registration
    "category_income" -> R.string.category_income
    "category_salary" -> R.string.category_salary
    "category_personal" -> R.string.category_personal
    "category_drugstore" -> R.string.category_drugstore
    "category_pharmacy" -> R.string.category_pharmacy
    "category_shopping" -> R.string.category_shopping
    "category_clothing" -> R.string.category_clothing
    "category_online" -> R.string.category_online
    "category_electronics" -> R.string.category_electronics
    "category_home" -> R.string.category_home
    "category_leisure" -> R.string.category_leisure
    "category_entertainment" -> R.string.category_entertainment
    "category_subscriptions" -> R.string.category_subscriptions
    else -> R.string.uncategorized
}

private fun parseAmountToMinor(value: String): Long? = runCatching {
    BigDecimal(value.trim().replace(',', '.'))
        .movePointRight(2)
        .setScale(0, RoundingMode.HALF_UP)
        .longValueExact()
}.getOrNull()

private fun formatEditableAmount(amountMinor: Long): String =
    BigDecimal.valueOf(amountMinor, 2)
        .stripTrailingZeros()
        .toPlainString()

private fun formatMoney(amountMinor: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "${formatter.format(BigDecimal.valueOf(amountMinor, 2))} RSD"
}

private fun formatDate(epochDay: Long): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
        .format(LocalDate.ofEpochDay(epochDay))

private fun Long.toDatePickerMillis(): Long =
    LocalDate.ofEpochDay(this)
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

private fun Long.datePickerMillisToEpochDay(): Long =
    Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toEpochDay()

private fun currentAppLanguage(): AppLanguage {
    val applicationTag = AppCompatDelegate
        .getApplicationLocales()
        .toLanguageTags()

    if (applicationTag.startsWith("sr", ignoreCase = true)) {
        return AppLanguage.SERBIAN_LATIN
    }

    if (applicationTag.startsWith("en", ignoreCase = true)) {
        return AppLanguage.ENGLISH
    }

    return if (Locale.getDefault().language.equals("sr", ignoreCase = true)) {
        AppLanguage.SERBIAN_LATIN
    } else {
        AppLanguage.ENGLISH
    }
}
