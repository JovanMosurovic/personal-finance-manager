package com.jovanmosurovic.personalfinancemanager.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Category
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
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
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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
                Surface(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                    )
                ) {
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
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
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
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
    ) { innerPadding ->
        if (!uiState.isReady) {
            LoadingScreen(modifier = Modifier.padding(innerPadding))
        } else if (showAddTransaction) {
            AddTransactionScreen(
                modifier = Modifier.padding(innerPadding),
                onCancel = { showAddTransaction = false },
                onSave = { type, amountMinor, merchant, note ->
                    financeViewModel.addTransaction(type, amountMinor, merchant, note)
                    showAddTransaction = false
                }
            )
        } else {
            when (selectedRoute) {
                TopLevelDestination.TRANSACTIONS.route -> TransactionsScreen(
                    uiState = uiState,
                    onAddTransaction = { showAddTransaction = true },
                    onAssignCategory = financeViewModel::assignCategory,
                    modifier = Modifier.padding(innerPadding)
                )

                TopLevelDestination.ANALYTICS.route -> AnalyticsScreen(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )

                TopLevelDestination.CATEGORIES.route -> CategoriesScreen(
                    uiState = uiState,
                    onAddKeyword = financeViewModel::addKeyword,
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
    modifier: Modifier = Modifier
) {
    val points = weeklySpendingPoints(transactions)
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
            val gap = 7.dp.toPx()
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
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { point ->
                Text(
                    text = chartDayLabel(point.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun weeklySpendingPoints(transactions: List<TransactionEntity>): List<SpendingPoint> {
    val today = LocalDate.now()
    return (6 downTo 0).map { daysAgo ->
        val date = today.minusDays(daysAgo.toLong())
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

private fun chartDayLabel(date: LocalDate): String =
    DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        .format(date)
        .replace(".", "")
        .take(3)
        .uppercase(Locale.getDefault())

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
            color = accentColor
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
    modifier: Modifier = Modifier
) {
    var showOnlyUncategorized by rememberSaveable { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
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
        CategoryAssignmentDialog(
            transaction = transaction,
            categories = uiState.categories,
            onDismiss = { selectedTransaction = null },
            onConfirm = { categoryId, saveMerchantAsKeyword ->
                onAssignCategory(transaction.id, categoryId, saveMerchantAsKeyword)
                selectedTransaction = null
            }
        )
    }
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
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = category?.let { categoryLabel(it) }
                        ?: stringResource(R.string.no_category_assigned),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(transaction.dateEpochDay),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                color = amountColor
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
            Column {
                Text(
                    text = transaction.merchant,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(categories, key = { it.id }) { category ->
                        val parent = categories.firstOrNull { it.id == category.parentId }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategoryId = category.id }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategoryId == category.id,
                                onClick = { selectedCategoryId = category.id }
                            )
                            Column {
                                Text(text = categoryLabel(category))
                                if (parent != null) {
                                    Text(
                                        text = categoryLabel(parent),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                    Checkbox(
                        checked = saveMerchantAsKeyword,
                        onCheckedChange = { saveMerchantAsKeyword = it }
                    )
                    Text(stringResource(R.string.remember_keyword))
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
    modifier: Modifier = Modifier
) {
    var categoryForKeyword by remember { mutableStateOf<CategoryEntity?>(null) }
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
                onAddKeyword = { categoryForKeyword = it }
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
}

@Composable
private fun CategoryGroup(
    topLevelCategory: CategoryEntity,
    children: List<CategoryEntity>,
    keywordRules: List<com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity>,
    onAddKeyword: (CategoryEntity) -> Unit
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
                KeywordChips(topLevelKeywords)
            }
            children.forEach { category ->
                CategoryKeywordRow(
                    category = category,
                    keywords = keywordRules.filter { it.categoryId == category.id },
                    onAddKeyword = { onAddKeyword(category) }
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
    keywords: List<com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity>,
    onAddKeyword: () -> Unit
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
            KeywordChips(keywords)
        }
    }
}

@Composable
private fun KeywordChips(
    keywords: List<com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity>
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(keywords, key = { it.id }) { rule ->
            AssistChip(
                onClick = {},
                label = { Text(rule.keyword) }
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
    val netThisMonth = uiState.incomeThisMonthMinor - uiState.expensesThisMonthMinor
    val categorySpending = monthlyCategorySpending(uiState.transactions)
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.net_this_month),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatMoney(netThisMonth),
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
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                SpendingChart(
                    transactions = uiState.transactions,
                    emptyLabel = stringResource(R.string.analytics_placeholder)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.transactions_count, uiState.transactions.size),
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

private fun monthlyCategorySpending(
    transactions: List<TransactionEntity>
): List<CategorySpending> {
    val monthStart = LocalDate.now().withDayOfMonth(1).toEpochDay()
    return transactions
        .asSequence()
        .filter {
            it.type == TransactionType.EXPENSE.name &&
                it.dateEpochDay >= monthStart
        }
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
    onSave: (TransactionType, Long, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTypeName by rememberSaveable { mutableStateOf(TransactionType.EXPENSE.name) }
    var amount by rememberSaveable { mutableStateOf("") }
    var merchant by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
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
                    onSave(selectedType, amountMinor, merchant, note)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save))
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
        Row(modifier = Modifier.padding(4.dp)) {
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

@Composable
private fun TransactionTypeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .semantics { role = Role.RadioButton },
        shape = MaterialTheme.shapes.small,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp),
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
