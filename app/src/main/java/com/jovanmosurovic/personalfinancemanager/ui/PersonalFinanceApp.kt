package com.jovanmosurovic.personalfinancemanager.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.Sort
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.data.importer.OtpImportFormat
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionNameFormatter
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
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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
    THIS_MONTH(R.string.analytics_period_this_month),
    CUSTOM(R.string.filter_period_custom)
}

private enum class AnalyticsCustomPeriodMode {
    WHOLE_MONTH,
    DATE_RANGE
}

private enum class AnalyticsComparisonMode(
    val labelRes: Int
) {
    SAME_DURATION(R.string.analytics_same_duration),
    PREVIOUS_MONTH(R.string.analytics_previous_month),
    SELECTED_MONTH(R.string.analytics_selected_month)
}

private data class AnalyticsDateRange(
    val start: LocalDate,
    val end: LocalDate
) {
    val dayCount: Int
        get() = ChronoUnit.DAYS.between(start, end).toInt() + 1
}

private enum class TransactionTypeFilter(val labelRes: Int) {
    ALL(R.string.filter_type_all),
    EXPENSE(R.string.add_expense),
    INCOME(R.string.add_income)
}

private enum class TransactionDateFilter(val labelRes: Int) {
    ALL_TIME(R.string.filter_period_all),
    LAST_7_DAYS(R.string.analytics_period_7_days),
    LAST_30_DAYS(R.string.analytics_period_30_days),
    THIS_MONTH(R.string.analytics_period_this_month),
    MONTH(R.string.filter_period_month),
    CUSTOM(R.string.filter_period_custom)
}

private enum class TransactionSort(val labelRes: Int) {
    NEWEST(R.string.sort_newest),
    OLDEST(R.string.sort_oldest),
    HIGHEST_AMOUNT(R.string.sort_highest_amount),
    LOWEST_AMOUNT(R.string.sort_lowest_amount)
}

private const val ALL_CATEGORIES_FILTER = Long.MIN_VALUE
private const val UNCATEGORIZED_FILTER = 0L
private const val TRANSACTIONS_ROUTE = "transactions"
private const val ADD_TRANSACTION_ROUTE = "add_transaction"

@Composable
fun PersonalFinanceApp() {
    val navController = rememberNavController()
    val financeViewModel: FinanceViewModel = hiltViewModel()
    val uiState by financeViewModel.uiState.collectAsStateWithLifecycle()
    val importState by financeViewModel.importState.collectAsStateWithLifecycle()
    val areAmountsHidden by financeViewModel.areAmountsHidden.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val isAddTransactionRoute = currentRoute == ADD_TRANSACTION_ROUTE
    var transactionInitialCategoryId by rememberSaveable {
        mutableStateOf(ALL_CATEGORIES_FILTER)
    }
    var transactionInitialTypeFilterName by rememberSaveable {
        mutableStateOf(TransactionTypeFilter.ALL.name)
    }
    var transactionInitialDateStartEpochDay by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    var transactionInitialDateEndEpochDay by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    val resetTransactionNavigationFilter = {
        transactionInitialCategoryId = ALL_CATEGORIES_FILTER
        transactionInitialTypeFilterName = TransactionTypeFilter.ALL.name
        transactionInitialDateStartEpochDay = null
        transactionInitialDateEndEpochDay = null
    }
    val openTransactionsWithFilters: (
        Long?,
        TransactionTypeFilter,
        Long?,
        Long?
    ) -> Unit = { categoryId, typeFilter, start, end ->
        transactionInitialCategoryId = categoryId ?: ALL_CATEGORIES_FILTER
        transactionInitialTypeFilterName = typeFilter.name
        transactionInitialDateStartEpochDay = start
        transactionInitialDateEndEpochDay = end
        navController.navigate(TRANSACTIONS_ROUTE) {
            popUpTo(TopLevelDestination.DASHBOARD.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!isAddTransactionRoute) {
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
                                val isDestinationSelected = if (
                                    destination == TopLevelDestination.TRANSACTIONS
                                ) {
                                    currentRoute == TRANSACTIONS_ROUTE
                                } else {
                                    currentRoute == destination.route
                                }
                                NavigationBarItem(
                                    selected = isDestinationSelected,
                                    onClick = {
                                        if (destination == TopLevelDestination.ANALYTICS) {
                                            if (currentRoute != destination.route) {
                                                val returnedToAnalytics = navController.popBackStack(
                                                    destination.route,
                                                    inclusive = false
                                                )
                                                if (!returnedToAnalytics) {
                                                    navController.navigate(destination.route) {
                                                        popUpTo(TopLevelDestination.DASHBOARD.route) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            }
                                        } else {
                                            if (destination == TopLevelDestination.TRANSACTIONS) {
                                                resetTransactionNavigationFilter()
                                            }
                                            navController.navigate(destination.route) {
                                                popUpTo(TopLevelDestination.DASHBOARD.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = destination != TopLevelDestination.TRANSACTIONS
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (isDestinationSelected) {
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
        } else {
            NavHost(
                navController = navController,
                startDestination = TopLevelDestination.DASHBOARD.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(TopLevelDestination.DASHBOARD.route) {
                    DashboardScreen(
                        uiState = uiState,
                        areAmountsHidden = areAmountsHidden,
                        onAmountsVisibilityChanged = financeViewModel::setAmountsHidden,
                        onViewTransactions = openTransactionsWithFilters
                    )
                }
                composable(TRANSACTIONS_ROUTE) {
                    TransactionsScreen(
                        uiState = uiState,
                        areAmountsHidden = areAmountsHidden,
                        initialCategoryId = transactionInitialCategoryId,
                        initialTypeFilter = TransactionTypeFilter.entries.firstOrNull {
                            it.name == transactionInitialTypeFilterName
                        } ?: TransactionTypeFilter.ALL,
                        initialDateStartEpochDay = transactionInitialDateStartEpochDay,
                        initialDateEndEpochDay = transactionInitialDateEndEpochDay,
                        onAddTransaction = {
                            navController.navigate(ADD_TRANSACTION_ROUTE)
                        },
                        onAssignCategory = financeViewModel::assignCategory,
                        onUpdateTransaction = financeViewModel::updateTransaction,
                        onDeleteTransaction = financeViewModel::deleteTransaction
                    )
                }
                composable(TopLevelDestination.ANALYTICS.route) {
                    AnalyticsScreen(
                        uiState = uiState,
                        areAmountsHidden = areAmountsHidden,
                        onViewAllTransactions = { categoryId, typeFilter, start, end ->
                            openTransactionsWithFilters(categoryId, typeFilter, start, end)
                        }
                    )
                }
                composable(TopLevelDestination.CATEGORIES.route) {
                    CategoriesScreen(
                        uiState = uiState,
                        onAddKeyword = financeViewModel::addKeyword,
                        onDeleteKeyword = financeViewModel::deleteKeyword,
                        onAddCategory = financeViewModel::addCategory,
                        onRenameCategory = financeViewModel::renameCategory,
                        onDeleteCategory = financeViewModel::deleteCategory
                    )
                }
                composable(TopLevelDestination.SETTINGS.route) {
                    SettingsScreen(
                        importState = importState,
                        areAmountsHidden = areAmountsHidden,
                        onAmountsVisibilityChanged = financeViewModel::setAmountsHidden,
                        onImportFile = financeViewModel::importStatement,
                        onDismissImport = financeViewModel::clearImportState
                    )
                }
                composable(ADD_TRANSACTION_ROUTE) {
                    AddTransactionScreen(
                        onCancel = { navController.popBackStack() },
                        onSave = { type, amountMinor, merchant, note, dateEpochDay ->
                            financeViewModel.addTransaction(
                                type = type,
                                amountMinor = amountMinor,
                                merchant = merchant,
                                note = note,
                                dateEpochDay = dateEpochDay
                            )
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    uiState: FinanceUiState,
    areAmountsHidden: Boolean,
    onAmountsVisibilityChanged: (Boolean) -> Unit,
    onViewTransactions: (
        categoryId: Long?,
        typeFilter: TransactionTypeFilter,
        dateStartEpochDay: Long?,
        dateEndEpochDay: Long?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonthRange = AnalyticsPeriod.THIS_MONTH.dateRange(LocalDate.now())

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
            IconButton(
                onClick = { onAmountsVisibilityChanged(!areAmountsHidden) }
            ) {
                Icon(
                    imageVector = if (areAmountsHidden) {
                        Icons.Outlined.VisibilityOff
                    } else {
                        Icons.Outlined.Visibility
                    },
                    contentDescription = stringResource(
                        if (areAmountsHidden) {
                            R.string.show_amounts
                        } else {
                            R.string.hide_amounts
                        }
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                    modifier = Modifier.amountBlur(areAmountsHidden),
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
                accentColor = MaterialTheme.colorScheme.secondary,
                valueBlurred = areAmountsHidden,
                    onClick = {
                        onViewTransactions(
                            null,
                            TransactionTypeFilter.INCOME,
                            currentMonthRange.start.toEpochDay(),
                            currentMonthRange.end.toEpochDay()
                        )
                    }
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.expenses_this_month),
                value = formatMoney(uiState.expensesThisMonthMinor),
                accentColor = MaterialTheme.colorScheme.tertiary,
                valueBlurred = areAmountsHidden,
                    onClick = {
                        onViewTransactions(
                            null,
                            TransactionTypeFilter.EXPENSE,
                            currentMonthRange.start.toEpochDay(),
                            currentMonthRange.end.toEpochDay()
                        )
                    }
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
                    emptyLabel = stringResource(R.string.expense_chart_placeholder),
                    areAmountsHidden = areAmountsHidden,
                    onDateSelected = { date ->
                        onViewTransactions(
                            null,
                            TransactionTypeFilter.ALL,
                            date.toEpochDay(),
                            date.toEpochDay()
                        )
                    }
                )
            }
        }

        RecentActivityCard(
            transactions = uiState.transactions,
            categories = uiState.categories,
            areAmountsHidden = areAmountsHidden,
            onViewAll = {
                onViewTransactions(
                    null,
                    TransactionTypeFilter.ALL,
                    null,
                    null
                )
            }
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    valueBlurred: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick == null) {
        modifier
    } else {
        modifier.clickable(role = Role.Button, onClick = onClick)
    }

    Card(
        modifier = cardModifier,
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
            Text(
                text = value,
                modifier = Modifier.amountBlur(valueBlurred),
                style = MaterialTheme.typography.titleMedium
            )
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
    modifier: Modifier = Modifier,
    period: AnalyticsPeriod = AnalyticsPeriod.LAST_7_DAYS,
    range: AnalyticsDateRange? = null,
    areAmountsHidden: Boolean = false,
    selectedDate: LocalDate? = null,
    onDateSelected: ((LocalDate) -> Unit)? = null
) {
    val points = spendingPointsForPeriod(transactions, period, range)
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

    if (areAmountsHidden) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(128.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.VisibilityOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.amounts_hidden_chart),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .pointerInput(points, onDateSelected) {
                    if (onDateSelected == null) return@pointerInput

                    detectTapGestures { tapOffset ->
                        val gap = if (points.size > 14) 3.dp.toPx() else 7.dp.toPx()
                        val barWidth = (size.width - gap * (points.size - 1)) / points.size
                        val slotWidth = barWidth + gap
                        val index = (tapOffset.x / slotWidth)
                            .roundToInt()
                            .coerceIn(0, points.lastIndex)
                        onDateSelected(points[index].date)
                    }
                }
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
                val isSelected = selectedDate == point.date
                val barColor = when {
                    isSelected -> primaryColor
                    selectedDate != null -> expenseColor.copy(alpha = 0.68f)
                    index == points.lastIndex -> primaryColor
                    else -> expenseColor
                }
                drawRoundRect(
                    color = barColor,
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
    period: AnalyticsPeriod,
    range: AnalyticsDateRange? = null
): List<SpendingPoint> {
    val today = LocalDate.now()
    val resolvedRange = range ?: period.dateRange(today)

    return (0 until resolvedRange.dayCount).map { dayOffset ->
        val date = resolvedRange.start.plusDays(dayOffset.toLong())
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

private fun AnalyticsPeriod.dateRange(
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

private fun AnalyticsPeriod.comparisonRange(
    today: LocalDate,
    mode: AnalyticsComparisonMode,
    selectedMonth: LocalDate = today.minusMonths(1).withDayOfMonth(1),
    currentRange: AnalyticsDateRange? = null
): AnalyticsDateRange {
    if (mode == AnalyticsComparisonMode.PREVIOUS_MONTH) {
        val previousMonth = today.minusMonths(1)
        return AnalyticsDateRange(
            start = previousMonth.withDayOfMonth(1),
            end = previousMonth.withDayOfMonth(previousMonth.lengthOfMonth())
        )
    }

    if (mode == AnalyticsComparisonMode.SELECTED_MONTH) {
        val normalizedMonth = selectedMonth.withDayOfMonth(1)
        return AnalyticsDateRange(
            start = normalizedMonth,
            end = normalizedMonth.withDayOfMonth(normalizedMonth.lengthOfMonth())
        )
    }

    if (this == AnalyticsPeriod.THIS_MONTH) {
        val previousMonth = today.minusMonths(1)
        val previousMonthEndDay = minOf(today.dayOfMonth, previousMonth.lengthOfMonth())
        return AnalyticsDateRange(
            start = previousMonth.withDayOfMonth(1),
            end = previousMonth.withDayOfMonth(previousMonthEndDay)
        )
    }

    val range = currentRange ?: dateRange(today)
    val comparisonEnd = range.start.minusDays(1)
    return AnalyticsDateRange(
        start = comparisonEnd.minusDays(range.dayCount.toLong() - 1),
        end = comparisonEnd
    )
}

private fun availableMonthsUntil(
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

private fun availableComparisonMonths(
    transactions: List<TransactionEntity>,
    today: LocalDate
): List<LocalDate> = availableMonthsUntil(
    transactions = transactions,
    latestMonth = today.withDayOfMonth(1).minusMonths(1)
)

private fun transactionsInPeriod(
    transactions: List<TransactionEntity>,
    period: AnalyticsPeriod,
    today: LocalDate = LocalDate.now(),
    customRange: AnalyticsDateRange? = null
): List<TransactionEntity> {
    return transactionsInRange(transactions, period.dateRange(today, customRange))
}

private fun transactionsInRange(
    transactions: List<TransactionEntity>,
    range: AnalyticsDateRange
): List<TransactionEntity> {
    val startEpochDay = range.start.toEpochDay()
    val endEpochDay = range.end.toEpochDay()
    return transactions.filter { it.dateEpochDay in startEpochDay..endEpochDay }
}

private fun formatDateRange(range: AnalyticsDateRange): String =
    "${formatDate(range.start.toEpochDay())} - ${formatDate(range.end.toEpochDay())}"

private fun percentageChange(current: Long, previous: Long): String {
    if (previous == 0L) return "0%"

    val percentage = ((current - previous).toDouble() / previous.toDouble()) * 100.0
    return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(percentage.absoluteValue) + "%"
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
                    style = MaterialTheme.typography.bodyMedium,
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
    category: CategoryEntity?,
    areAmountsHidden: Boolean
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
                text = TransactionNameFormatter.displayName(transaction.merchant),
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
            modifier = Modifier.amountBlur(areAmountsHidden),
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
    areAmountsHidden: Boolean,
    modifier: Modifier = Modifier,
    initialCategoryId: Long = ALL_CATEGORIES_FILTER,
    initialTypeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    initialDateStartEpochDay: Long? = null,
    initialDateEndEpochDay: Long? = null,
    onAddTransaction: () -> Unit,
    onAssignCategory: (Long, Long, Boolean) -> Unit,
    onUpdateTransaction: (Long, TransactionType, Long, String, String, Long) -> Unit,
    onDeleteTransaction: (Long) -> Unit
) {
    var searchQuery by rememberSaveable(
        initialCategoryId,
        initialTypeFilter.name,
        initialDateStartEpochDay,
        initialDateEndEpochDay
    ) {
        mutableStateOf("")
    }
    var selectedTypeFilterName by rememberSaveable(initialTypeFilter.name) {
        mutableStateOf(initialTypeFilter.name)
    }
    var selectedCategoryId by rememberSaveable(initialCategoryId) {
        mutableStateOf(initialCategoryId)
    }
    val hasInitialDateFilter = initialDateStartEpochDay != null && initialDateEndEpochDay != null
    var selectedDateFilterName by rememberSaveable(
        initialDateStartEpochDay,
        initialDateEndEpochDay
    ) {
        mutableStateOf(
            if (hasInitialDateFilter) {
                TransactionDateFilter.CUSTOM.name
            } else {
                TransactionDateFilter.ALL_TIME.name
            }
        )
    }
    var selectedSortName by rememberSaveable(
        initialCategoryId,
        initialTypeFilter.name,
        initialDateStartEpochDay,
        initialDateEndEpochDay
    ) {
        mutableStateOf(TransactionSort.NEWEST.name)
    }
    var customStartEpochDay by rememberSaveable(
        initialDateStartEpochDay,
        initialDateEndEpochDay
    ) { mutableStateOf(initialDateStartEpochDay) }
    var customEndEpochDay by rememberSaveable(
        initialDateStartEpochDay,
        initialDateEndEpochDay
    ) { mutableStateOf(initialDateEndEpochDay) }
    var selectedMonthEpochDay by rememberSaveable(
        initialCategoryId,
        initialTypeFilter.name,
        initialDateStartEpochDay,
        initialDateEndEpochDay
    ) {
        mutableStateOf(LocalDate.now().withDayOfMonth(1).toEpochDay())
    }
    var showCustomDatePicker by rememberSaveable { mutableStateOf(false) }
    var showMonthPicker by rememberSaveable { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionForCategory by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionBeingEdited by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionPendingDeletion by remember { mutableStateOf<TransactionEntity?>(null) }

    val selectedTypeFilter = TransactionTypeFilter.entries.firstOrNull {
        it.name == selectedTypeFilterName
    } ?: TransactionTypeFilter.ALL
    val categoriesById = remember(uiState.categories) {
        uiState.categories.associateBy { it.id }
    }
    val selectedCategory = categoriesById[selectedCategoryId]
    val today = LocalDate.now()
    val selectedMonth = LocalDate.ofEpochDay(selectedMonthEpochDay).withDayOfMonth(1)
    val transactionMonths = remember(uiState.transactions, today) {
        availableMonthsUntil(
            transactions = uiState.transactions,
            latestMonth = today.withDayOfMonth(1)
        )
    }
    val selectedDateFilter = TransactionDateFilter.entries.firstOrNull {
        it.name == selectedDateFilterName
    } ?: TransactionDateFilter.ALL_TIME
    val selectedDateFilterLabel = when {
        selectedDateFilter == TransactionDateFilter.MONTH -> formatMonthYear(selectedMonth)
        selectedDateFilter == TransactionDateFilter.CUSTOM &&
            customStartEpochDay != null &&
            customEndEpochDay != null -> stringResource(
                R.string.filter_period_custom_range,
                formatDate(customStartEpochDay!!),
                formatDate(customEndEpochDay!!)
            )
        else -> stringResource(selectedDateFilter.labelRes)
    }
    val selectedSort = TransactionSort.entries.firstOrNull {
        it.name == selectedSortName
    } ?: TransactionSort.NEWEST
    val uncategorizedCount = remember(uiState.transactions) {
        uiState.transactions.count { it.categoryId == null }
    }
    val filteredTransactions = remember(
        uiState.transactions,
        searchQuery,
        selectedTypeFilter,
        selectedCategoryId,
        selectedDateFilter,
        selectedMonthEpochDay,
        customStartEpochDay,
        customEndEpochDay,
        today
    ) {
        filterTransactions(
            transactions = uiState.transactions,
            searchQuery = searchQuery,
            typeFilter = selectedTypeFilter,
            categoryId = selectedCategoryId,
            dateFilter = selectedDateFilter,
            selectedMonthEpochDay = selectedMonthEpochDay,
            customStartEpochDay = customStartEpochDay,
            customEndEpochDay = customEndEpochDay,
            today = today
        )
    }
    val visibleTransactions = remember(filteredTransactions, selectedSort) {
        sortTransactions(filteredTransactions, selectedSort)
    }
    val hasActiveFilters = searchQuery.isNotBlank() ||
        selectedTypeFilter != TransactionTypeFilter.ALL ||
        selectedCategoryId != ALL_CATEGORIES_FILTER ||
        selectedDateFilter != TransactionDateFilter.ALL_TIME ||
        selectedSort != TransactionSort.NEWEST
    val clearFilters = {
        searchQuery = ""
        selectedTypeFilterName = TransactionTypeFilter.ALL.name
        selectedCategoryId = ALL_CATEGORIES_FILTER
        selectedDateFilterName = TransactionDateFilter.ALL_TIME.name
        selectedMonthEpochDay = LocalDate.now().withDayOfMonth(1).toEpochDay()
        customStartEpochDay = null
        customEndEpochDay = null
        showCustomDatePicker = false
        showMonthPicker = false
        selectedSortName = TransactionSort.NEWEST.name
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        // The app-level Scaffold already accounts for the system and bottom navigation insets.
        // Applying them again here leaves an unnecessary gap above the bottom navigation bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(R.string.transactions_title),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = stringResource(R.string.transactions_count, visibleTransactions.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_transactions_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = stringResource(R.string.clear_search)
                                )
                            }
                        }
                    } else {
                        null
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    item {
                        TransactionFilterMenuChip(
                            label = stringResource(selectedTypeFilter.labelRes),
                            icon = Icons.Outlined.FilterList,
                            selected = selectedTypeFilter != TransactionTypeFilter.ALL,
                            menuContent = { closeMenu ->
                                TransactionTypeFilter.entries.forEach { filter ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(filter.labelRes)) },
                                        onClick = {
                                            selectedTypeFilterName = filter.name
                                            closeMenu()
                                        }
                                    )
                                }
                            }
                        )
                    }
                    item {
                        TransactionFilterMenuChip(
                            label = when {
                                selectedCategoryId == ALL_CATEGORIES_FILTER -> {
                                    stringResource(R.string.filter_category_all)
                                }
                                selectedCategoryId == UNCATEGORIZED_FILTER -> {
                                    stringResource(
                                        R.string.uncategorized_count,
                                        uncategorizedCount
                                    )
                                }
                                selectedCategory != null -> categoryLabel(selectedCategory)
                                else -> stringResource(R.string.filter_category_all)
                            },
                            icon = Icons.Outlined.Category,
                            selected = selectedCategoryId != ALL_CATEGORIES_FILTER,
                            menuContent = { closeMenu ->
                                DropdownMenuItem(
                                    text = {
                                        Text(stringResource(R.string.filter_category_all))
                                    },
                                    onClick = {
                                        selectedCategoryId = ALL_CATEGORIES_FILTER
                                        closeMenu()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(
                                                R.string.uncategorized_count,
                                                uncategorizedCount
                                            )
                                        )
                                    },
                                    onClick = {
                                        selectedCategoryId = UNCATEGORIZED_FILTER
                                        closeMenu()
                                    }
                                )
                                uiState.categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = categoryLabel(category),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                category.parentId?.let { parentId ->
                                                    categoriesById[parentId]?.let { parent ->
                                                        Text(
                                                            text = categoryLabel(parent),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedCategoryId = category.id
                                            closeMenu()
                                        }
                                    )
                                }
                            }
                        )
                    }
                    item {
                        TransactionFilterMenuChip(
                            label = selectedDateFilterLabel,
                            icon = Icons.Outlined.DateRange,
                            selected = selectedDateFilter != TransactionDateFilter.ALL_TIME,
                            menuContent = { closeMenu ->
                                TransactionDateFilter.entries.forEach { filter ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(filter.labelRes)) },
                                        onClick = {
                                            when (filter) {
                                                TransactionDateFilter.MONTH -> {
                                                    showMonthPicker = true
                                                }
                                                TransactionDateFilter.CUSTOM -> {
                                                    showCustomDatePicker = true
                                                }
                                                else -> {
                                                    selectedDateFilterName = filter.name
                                                }
                                            }
                                            closeMenu()
                                        }
                                    )
                                }
                            }
                        )
                    }
                    item {
                        TransactionFilterMenuChip(
                            label = stringResource(selectedSort.labelRes),
                            icon = Icons.AutoMirrored.Outlined.Sort,
                            selected = selectedSort != TransactionSort.NEWEST,
                            menuContent = { closeMenu ->
                                TransactionSort.entries.forEach { sort ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(sort.labelRes)) },
                                        onClick = {
                                            selectedSortName = sort.name
                                            closeMenu()
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
                if (hasActiveFilters) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = clearFilters,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.clear_filters))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (visibleTransactions.isEmpty() && uiState.transactions.isNotEmpty()) {
                    NoMatchingTransactionsContent(
                        showClearAction = hasActiveFilters,
                        onClear = clearFilters
                    )
                } else if (visibleTransactions.isEmpty()) {
                    EmptyTransactionsContent()
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(visibleTransactions, key = { it.id }) { transaction ->
                            TransactionRow(
                                transaction = transaction,
                                category = categoriesById[transaction.categoryId],
                                areAmountsHidden = areAmountsHidden,
                                onClick = { selectedTransaction = transaction }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(112.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.86f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
        }
    }

    selectedTransaction?.let { transaction ->
        TransactionDetailsDialog(
            transaction = transaction,
            category = categoriesById[transaction.categoryId],
            areAmountsHidden = areAmountsHidden,
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
            areAmountsHidden = areAmountsHidden,
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

    if (showCustomDatePicker) {
        CustomTransactionDateRangeDialog(
            initialStartEpochDay = customStartEpochDay,
            initialEndEpochDay = customEndEpochDay,
            onDismiss = { showCustomDatePicker = false },
            onConfirm = { startEpochDay, endEpochDay ->
                customStartEpochDay = minOf(startEpochDay, endEpochDay)
                customEndEpochDay = maxOf(startEpochDay, endEpochDay)
                selectedDateFilterName = TransactionDateFilter.CUSTOM.name
                showCustomDatePicker = false
            }
        )
    }

    if (showMonthPicker) {
        AnalyticsMonthPickerDialog(
            titleRes = R.string.select_transaction_month,
            initialMonth = selectedMonth,
            availableMonths = transactionMonths,
            onDismiss = { showMonthPicker = false },
            onConfirm = { month ->
                selectedMonthEpochDay = month.withDayOfMonth(1).toEpochDay()
                selectedDateFilterName = TransactionDateFilter.MONTH.name
                showMonthPicker = false
            }
        )
    }
}

@Composable
private fun TransactionFilterMenuChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    menuContent: @Composable (closeMenu: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = selected,
            onClick = { expanded = true },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            menuContent { expanded = false }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTransactionDateRangeDialog(
    initialStartEpochDay: Long?,
    initialEndEpochDay: Long?,
    onDismiss: () -> Unit,
    onConfirm: (startEpochDay: Long, endEpochDay: Long) -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartEpochDay?.toDatePickerMillis(),
        initialSelectedEndDateMillis = initialEndEpochDay?.toDatePickerMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val startEpochDay = dateRangePickerState.selectedStartDateMillis
                ?.datePickerMillisToEpochDay()
            val endEpochDay = dateRangePickerState.selectedEndDateMillis
                ?.datePickerMillisToEpochDay()

            TextButton(
                enabled = startEpochDay != null && endEpochDay != null,
                onClick = {
                    if (startEpochDay != null && endEpochDay != null) {
                        onConfirm(startEpochDay, endEpochDay)
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
    ) {
        val startDateText = dateRangePickerState.selectedStartDateMillis
            ?.datePickerMillisToEpochDay()
            ?.let(::formatDate)
            ?: stringResource(R.string.date_range_not_selected)
        val endDateText = dateRangePickerState.selectedEndDateMillis
            ?.datePickerMillisToEpochDay()
            ?.let(::formatDate)
            ?: stringResource(R.string.date_range_not_selected)

        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = stringResource(R.string.select_date_range),
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
                )
            },
            headline = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DateRangeSelectionCard(
                        label = stringResource(R.string.date_range_start),
                        value = startDateText,
                        modifier = Modifier.weight(1f)
                    )
                    DateRangeSelectionCard(
                        label = stringResource(R.string.date_range_end),
                        value = endDateText,
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                navigationContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary,
                dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            )
        )
    }
}

@Composable
private fun DateRangeSelectionCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun filterTransactions(
    transactions: List<TransactionEntity>,
    searchQuery: String,
    typeFilter: TransactionTypeFilter,
    categoryId: Long,
    dateFilter: TransactionDateFilter,
    selectedMonthEpochDay: Long,
    customStartEpochDay: Long?,
    customEndEpochDay: Long?,
    today: LocalDate = LocalDate.now()
): List<TransactionEntity> {
    val normalizedQuery = searchQuery.trim()
    val todayEpochDay = today.toEpochDay()

    return transactions.filter { transaction ->
        val matchesSearch = normalizedQuery.isBlank() ||
            transaction.merchant.contains(normalizedQuery, ignoreCase = true) ||
            TransactionNameFormatter.displayName(transaction.merchant)
                .contains(normalizedQuery, ignoreCase = true) ||
            transaction.note.contains(normalizedQuery, ignoreCase = true)

        val matchesType = when (typeFilter) {
            TransactionTypeFilter.ALL -> true
            TransactionTypeFilter.EXPENSE -> transaction.type == TransactionType.EXPENSE.name
            TransactionTypeFilter.INCOME -> transaction.type == TransactionType.INCOME.name
        }

        val matchesCategory = when (categoryId) {
            ALL_CATEGORIES_FILTER -> true
            UNCATEGORIZED_FILTER -> transaction.categoryId == null
            else -> transaction.categoryId == categoryId
        }

        val matchesDate = when (dateFilter) {
            TransactionDateFilter.ALL_TIME -> true
            TransactionDateFilter.LAST_7_DAYS -> transaction.dateEpochDay in
                today.minusDays(6).toEpochDay()..todayEpochDay
            TransactionDateFilter.LAST_30_DAYS -> transaction.dateEpochDay in
                today.minusDays(29).toEpochDay()..todayEpochDay
            TransactionDateFilter.THIS_MONTH -> transaction.dateEpochDay in
                today.withDayOfMonth(1).toEpochDay()..todayEpochDay
            TransactionDateFilter.MONTH -> {
                val month = LocalDate.ofEpochDay(selectedMonthEpochDay).withDayOfMonth(1)
                transaction.dateEpochDay in
                    month.toEpochDay()..month.withDayOfMonth(month.lengthOfMonth()).toEpochDay()
            }
            TransactionDateFilter.CUSTOM -> {
                customStartEpochDay != null &&
                    customEndEpochDay != null &&
                    transaction.dateEpochDay in customStartEpochDay..customEndEpochDay
            }
        }

        matchesSearch && matchesType && matchesCategory && matchesDate
    }
}

private fun sortTransactions(
    transactions: List<TransactionEntity>,
    sort: TransactionSort
): List<TransactionEntity> = when (sort) {
    TransactionSort.NEWEST -> transactions.sortedWith(
        compareByDescending<TransactionEntity> { it.dateEpochDay }
            .thenByDescending { it.id }
    )
    TransactionSort.OLDEST -> transactions.sortedWith(
        compareBy<TransactionEntity> { it.dateEpochDay }
            .thenBy { it.id }
    )
    TransactionSort.HIGHEST_AMOUNT -> transactions.sortedWith(
        compareByDescending<TransactionEntity> { it.amountMinor }
            .thenByDescending { it.dateEpochDay }
            .thenByDescending { it.id }
    )
    TransactionSort.LOWEST_AMOUNT -> transactions.sortedWith(
        compareBy<TransactionEntity> { it.amountMinor }
            .thenByDescending { it.dateEpochDay }
            .thenByDescending { it.id }
    )
}

@Composable
private fun TransactionDetailsDialog(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    areAmountsHidden: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onAssignCategory: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME.name
    val displayName = TransactionNameFormatter.displayName(transaction.merchant)
    val sourceDescription = TransactionNameFormatter.sourceDescription(transaction.merchant)
    val hasOriginalDescription = displayName != sourceDescription
    var showOriginalDescription by remember(transaction.id) { mutableStateOf(false) }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
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
                                    text = stringResource(R.string.transaction_name),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatMoney(
                                        if (isIncome) transaction.amountMinor else -transaction.amountMinor
                                    ),
                                    modifier = Modifier.amountBlur(areAmountsHidden),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = amountColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    if (hasOriginalDescription) {
                        TextButton(
                            onClick = { showOriginalDescription = !showOriginalDescription },
                            modifier = Modifier.align(Alignment.Start),
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    if (showOriginalDescription) {
                                        R.string.hide_original_description
                                    } else {
                                        R.string.show_original_description
                                    }
                                )
                            )
                        }
                        if (showOriginalDescription) {
                            TransactionDetailRow(
                                label = stringResource(R.string.original_description),
                                value = sourceDescription
                            )
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
private fun NoMatchingTransactionsContent(
    showClearAction: Boolean,
    onClear: () -> Unit
) {
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
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.no_matching_transactions),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.no_matching_transactions_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (showClearAction) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.clear_filters))
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    areAmountsHidden: Boolean,
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
                    text = TransactionNameFormatter.displayName(transaction.merchant),
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
                modifier = Modifier.amountBlur(areAmountsHidden),
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
    areAmountsHidden: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long, Boolean) -> Unit
) {
    var selectedCategoryId by rememberSaveable(transaction.id) {
        mutableStateOf(transaction.categoryId ?: 0L)
    }
    var saveMerchantAsKeyword by rememberSaveable(transaction.id) {
        mutableStateOf(false)
    }
    val categoriesById = remember(categories) {
        categories.associateBy { it.id }
    }
    val topLevelCategories = remember(categories) {
        categories.filter { it.parentId == null }
    }
    val childrenByParentId = remember(categories) {
        categories
            .filter { it.parentId != null }
            .groupBy { it.parentId }
    }
    val initialExpandedParentId = categoriesById[transaction.categoryId]?.parentId
    var expandedParentCategoryId by rememberSaveable(transaction.id) {
        mutableStateOf(initialExpandedParentId)
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
                                text = TransactionNameFormatter.displayName(transaction.merchant),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatMoney(
                                    if (isIncome) transaction.amountMinor else -transaction.amountMinor
                                ),
                                modifier = Modifier.amountBlur(areAmountsHidden),
                                style = MaterialTheme.typography.labelLarge,
                                color = amountColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (expandedParentCategoryId == null) {
                    Text(
                        text = stringResource(R.string.select_category),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
                        if (expandedParentCategoryId == null) {
                            items(
                                items = topLevelCategories,
                                key = { it.id },
                                contentType = { "top_level_category" }
                            ) { category ->
                                val children = childrenByParentId[category.id].orEmpty()
                                CategoryAssignmentOption(
                                    category = category,
                                    selected = selectedCategoryId == category.id,
                                    hasChildren = children.isNotEmpty(),
                                    onClick = {
                                        if (children.isEmpty()) {
                                            selectedCategoryId = category.id
                                        } else {
                                            expandedParentCategoryId = category.id
                                        }
                                    },
                                    onSelect = { selectedCategoryId = category.id },
                                    onExpand = children.takeIf { it.isNotEmpty() }?.let {
                                        { expandedParentCategoryId = category.id }
                                    }
                                )
                            }
                        } else {
                            val parentCategory = categoriesById[expandedParentCategoryId]
                            val children = childrenByParentId[expandedParentCategoryId].orEmpty()

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { expandedParentCategoryId = null }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                            contentDescription = stringResource(R.string.back)
                                        )
                                    }
                                    Text(
                                        text = parentCategory?.let { categoryLabel(it) }.orEmpty(),
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            parentCategory?.let { category ->
                                item(key = category.id, contentType = "parent_category") {
                                    CategoryAssignmentOption(
                                        category = category,
                                        selected = selectedCategoryId == category.id,
                                        subtitle = stringResource(R.string.main_category),
                                        onClick = { selectedCategoryId = category.id },
                                        onSelect = { selectedCategoryId = category.id }
                                    )
                                }
                            }
                            items(
                                items = children,
                                key = { it.id },
                                contentType = { "subcategory" }
                            ) { category ->
                                CategoryAssignmentOption(
                                    category = category,
                                    selected = selectedCategoryId == category.id,
                                    onClick = { selectedCategoryId = category.id },
                                    onSelect = { selectedCategoryId = category.id }
                                )
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
                        colors = financeSwitchColors()
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
private fun CategoryAssignmentOption(
    category: CategoryEntity,
    selected: Boolean,
    subtitle: String? = null,
    hasChildren: Boolean = false,
    onClick: () -> Unit,
    onSelect: () -> Unit,
    onExpand: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (selected) {
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
                selected = selected,
                onClick = onSelect
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryLabel(category),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (hasChildren && onExpand != null) {
                IconButton(onClick = onExpand) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.open_subcategories)
                    )
                }
            }
        }
    }
}

private data class CategoryEditorState(
    val category: CategoryEntity? = null,
    val parentCategory: CategoryEntity? = null
)

@Composable
private fun CategoriesScreen(
    uiState: FinanceUiState,
    onAddKeyword: (Long, String) -> Unit,
    onDeleteKeyword: (Long) -> Unit,
    onAddCategory: (String, Long?) -> Unit,
    onRenameCategory: (Long, String) -> Unit,
    onDeleteCategory: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryForKeyword by remember { mutableStateOf<CategoryEntity?>(null) }
    var keywordPendingDeletion by remember { mutableStateOf<KeywordRuleEntity?>(null) }
    var categoryEditor by remember { mutableStateOf<CategoryEditorState?>(null) }
    var categoryPendingDeletion by remember { mutableStateOf<CategoryEntity?>(null) }
    val topLevelCategories = remember(uiState.categories) {
        uiState.categories.filter { it.parentId == null }
    }
    val childrenByParentId = remember(uiState.categories) {
        uiState.categories
            .filter { it.parentId != null }
            .groupBy { it.parentId }
    }
    val keywordsByCategoryId = remember(uiState.keywordRules) {
        uiState.keywordRules.groupBy { it.categoryId }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 24.dp,
                end = 20.dp,
                bottom = 132.dp
            )
        ) {
            item(key = "categories_title", contentType = "categories_header") {
                Text(
                    text = stringResource(R.string.categories_title),
                    modifier = Modifier.padding(bottom = 18.dp),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            item(key = "categories_description", contentType = "categories_header") {
                Text(
                    text = stringResource(R.string.categories_description),
                    modifier = Modifier.padding(bottom = 18.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            topLevelCategories.forEachIndexed { groupIndex, topLevelCategory ->
                val children = childrenByParentId[topLevelCategory.id].orEmpty()
                val isLastGroup = groupIndex == topLevelCategories.lastIndex

                item(
                    key = topLevelCategory.id,
                    contentType = "category_group_header"
                ) {
                    CategoryGroupHeader(
                        topLevelCategory = topLevelCategory,
                        keywords = keywordsByCategoryId[topLevelCategory.id].orEmpty(),
                        hasChildren = children.isNotEmpty(),
                        addGroupSpacing = children.isEmpty() && !isLastGroup,
                        onAddKeyword = { categoryForKeyword = it },
                        onDeleteKeyword = { keywordPendingDeletion = it },
                        onAddSubcategory = {
                            categoryEditor = CategoryEditorState(parentCategory = it)
                        },
                        onEditCategory = {
                            categoryEditor = CategoryEditorState(category = it)
                        },
                        onDeleteCategory = { categoryPendingDeletion = it }
                    )
                }

                children.forEachIndexed { childIndex, category ->
                    val isLastChild = childIndex == children.lastIndex
                    item(
                        key = category.id,
                        contentType = "category_group_child"
                    ) {
                        CategoryGroupChild(
                            category = category,
                            keywords = keywordsByCategoryId[category.id].orEmpty(),
                            isLastChild = isLastChild,
                            addGroupSpacing = isLastChild && !isLastGroup,
                            onAddKeyword = { categoryForKeyword = category },
                            onDeleteKeyword = { keywordPendingDeletion = it },
                            onEditCategory = {
                                categoryEditor = CategoryEditorState(category = category)
                            },
                            onDeleteCategory = { categoryPendingDeletion = category }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(112.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.86f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        SmallFloatingActionButton(
            onClick = { categoryEditor = CategoryEditorState() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 28.dp, bottom = 16.dp),
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.add_category),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.add_category),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }
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

    categoryEditor?.let { editor ->
        CategoryEditorDialog(
            category = editor.category,
            parentCategory = editor.parentCategory,
            onDismiss = { categoryEditor = null },
            onConfirm = { name ->
                if (editor.category != null) {
                    onRenameCategory(editor.category.id, name)
                } else {
                    onAddCategory(name, editor.parentCategory?.id)
                }
                categoryEditor = null
            }
        )
    }

    categoryPendingDeletion?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryPendingDeletion = null },
            title = {
                Text(stringResource(R.string.delete_category_title))
            },
            text = {
                Text(
                    stringResource(
                        R.string.delete_category_confirmation,
                        categoryLabel(category)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory(category.id)
                        categoryPendingDeletion = null
                    }
                ) {
                    Text(stringResource(R.string.delete_category))
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryPendingDeletion = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun CategoryEditorDialog(
    category: CategoryEntity?,
    parentCategory: CategoryEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by rememberSaveable(category?.id, parentCategory?.id) {
        mutableStateOf(category?.nameKey.orEmpty())
    }
    val title = when {
        category != null -> stringResource(R.string.edit_category)
        parentCategory != null -> stringResource(
            R.string.add_subcategory_title,
            categoryLabel(parentCategory)
        )
        else -> stringResource(R.string.add_category)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.category_name)) },
                placeholder = { Text(stringResource(R.string.category_name_hint)) },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim()) },
                enabled = name.trim().isNotEmpty()
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
private fun CategoryGroupHeader(
    topLevelCategory: CategoryEntity,
    keywords: List<KeywordRuleEntity>,
    hasChildren: Boolean,
    addGroupSpacing: Boolean,
    onAddKeyword: (CategoryEntity) -> Unit,
    onDeleteKeyword: (KeywordRuleEntity) -> Unit,
    onAddSubcategory: (CategoryEntity) -> Unit,
    onEditCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    val shape = if (hasChildren) {
        MaterialTheme.shapes.medium.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp)
        )
    } else {
        MaterialTheme.shapes.medium
    }

    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = if (hasChildren) 12.dp else 16.dp
                ),
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
                    if (!topLevelCategory.isSystem) {
                        IconButton(onClick = { onEditCategory(topLevelCategory) }) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.edit_category)
                            )
                        }
                        IconButton(onClick = { onDeleteCategory(topLevelCategory) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete_category)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onAddKeyword(topLevelCategory) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.add_keyword))
                    }
                    TextButton(
                        onClick = { onAddSubcategory(topLevelCategory) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.add_subcategory))
                    }
                }
                if (keywords.isNotEmpty()) {
                    KeywordChips(keywords, onDeleteKeyword)
                }
            }
        }
        if (addGroupSpacing) {
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun CategoryGroupChild(
    category: CategoryEntity,
    keywords: List<KeywordRuleEntity>,
    isLastChild: Boolean,
    addGroupSpacing: Boolean,
    onAddKeyword: () -> Unit,
    onDeleteKeyword: (KeywordRuleEntity) -> Unit,
    onEditCategory: () -> Unit,
    onDeleteCategory: () -> Unit
) {
    val shape = if (isLastChild) {
        MaterialTheme.shapes.medium.copy(
            topStart = CornerSize(0.dp),
            topEnd = CornerSize(0.dp)
        )
    } else {
        MaterialTheme.shapes.medium.copy(
            topStart = CornerSize(0.dp),
            topEnd = CornerSize(0.dp),
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp)
        )
    }

    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = if (isLastChild) 16.dp else 12.dp
                )
            ) {
                CategoryKeywordRow(
                    category = category,
                    keywords = keywords,
                    onAddKeyword = onAddKeyword,
                    onDeleteKeyword = onDeleteKeyword,
                    onEditCategory = onEditCategory,
                    onDeleteCategory = onDeleteCategory
                )
            }
        }
        if (addGroupSpacing) {
            Spacer(modifier = Modifier.height(18.dp))
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
                text = categoryLabel(category).take(1).uppercase(),
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
    onDeleteKeyword: (KeywordRuleEntity) -> Unit,
    onEditCategory: () -> Unit,
    onDeleteCategory: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryLabel(category),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!category.isSystem) {
                    IconButton(onClick = onEditCategory) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.edit_category)
                        )
                    }
                    IconButton(onClick = onDeleteCategory) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.delete_category)
                        )
                    }
                }
            }
            TextButton(
                onClick = onAddKeyword,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.add_keyword))
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
}

@Composable
private fun KeywordChips(
    keywords: List<KeywordRuleEntity>,
    onDeleteKeyword: (KeywordRuleEntity) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 4.dp)
    ) {
        items(
            items = keywords,
            key = { it.id },
            contentType = { "keyword" }
        ) { rule ->
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
    areAmountsHidden: Boolean,
    onViewAllTransactions: (
        categoryId: Long?,
        typeFilter: TransactionTypeFilter,
        dateStartEpochDay: Long?,
        dateEndEpochDay: Long?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriodName by rememberSaveable {
        mutableStateOf(AnalyticsPeriod.LAST_7_DAYS.name)
    }
    var comparisonModeName by rememberSaveable {
        mutableStateOf(AnalyticsComparisonMode.SAME_DURATION.name)
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
    var customPeriodModeName by rememberSaveable {
        mutableStateOf(AnalyticsCustomPeriodMode.WHOLE_MONTH.name)
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
    val selectedPeriod = AnalyticsPeriod.valueOf(selectedPeriodName)
    val comparisonMode = AnalyticsComparisonMode.valueOf(comparisonModeName)
    val customPeriodMode = AnalyticsCustomPeriodMode.valueOf(customPeriodModeName)
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
    val periodTransactions = transactionsInPeriod(
        transactions = uiState.transactions,
        period = selectedPeriod,
        today = today,
        customRange = customRange.takeIf { selectedPeriod == AnalyticsPeriod.CUSTOM }
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
    val maxCategorySpend = categorySpending.maxOfOrNull { it.amountMinor } ?: 0L
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            AnalyticsPeriod.entries.forEach { period ->
                item(key = period.name) {
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = {
                            selectedPeriodName = period.name
                            comparisonModeName = AnalyticsComparisonMode.SAME_DURATION.name
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
            maxCategorySpend = maxCategorySpend,
            areAmountsHidden = areAmountsHidden,
            onCategorySelected = {
                selectedCategoryId = it
                showCategoryDetails = true
            }
        )

        AnalyticsComparisonCard(
            period = selectedPeriod,
            comparisonMode = comparisonMode,
            onComparisonModeChanged = { comparisonModeName = it.name },
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
        AnalyticsMonthPickerDialog(
            titleRes = R.string.analytics_choose_analytics_month,
            initialMonth = customMonth,
            availableMonths = customMonths,
            onDismiss = { showCustomMonthPicker = false },
            onConfirm = { selectedMonth ->
                customMonthEpochDay = selectedMonth.withDayOfMonth(1).toEpochDay()
                customPeriodModeName = AnalyticsCustomPeriodMode.WHOLE_MONTH.name
                showCustomMonthPicker = false
            }
        )
    }

    if (showCustomRangePicker) {
        CustomTransactionDateRangeDialog(
            initialStartEpochDay = customStartEpochDay,
            initialEndEpochDay = customEndEpochDay,
            onDismiss = { showCustomRangePicker = false },
            onConfirm = { startEpochDay, endEpochDay ->
                customStartEpochDay = minOf(startEpochDay, endEpochDay)
                customEndEpochDay = maxOf(startEpochDay, endEpochDay)
                customPeriodModeName = AnalyticsCustomPeriodMode.DATE_RANGE.name
                showCustomRangePicker = false
            }
        )
    }

    if (showComparisonMonthPicker) {
        AnalyticsMonthPickerDialog(
            titleRes = R.string.analytics_choose_comparison_month,
            initialMonth = comparisonMonth,
            availableMonths = comparisonMonths,
            onDismiss = { showComparisonMonthPicker = false },
            onConfirm = { selectedMonth ->
                comparisonMonthEpochDay = selectedMonth.withDayOfMonth(1).toEpochDay()
                comparisonModeName = AnalyticsComparisonMode.SELECTED_MONTH.name
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

@Composable
private fun AnalyticsCategorySpendingCard(
    categorySpending: List<CategorySpending>,
    categories: List<CategoryEntity>,
    maxCategorySpend: Long,
    areAmountsHidden: Boolean,
    onCategorySelected: (Long?) -> Unit
) {
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
private fun AnalyticsCustomPeriodOptionsDialog(
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
private fun AnalyticsMonthPickerDialog(
    titleRes: Int,
    initialMonth: LocalDate,
    availableMonths: List<LocalDate>,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    var selectedMonthEpochDay by rememberSaveable(initialMonth.toEpochDay()) {
        mutableStateOf(initialMonth.toEpochDay())
    }
    val months = (availableMonths + initialMonth)
        .distinctBy { it.toEpochDay() }
        .sortedDescending()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(titleRes))
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    items = months,
                    key = { it.toEpochDay() }
                ) { month ->
                    val isSelected = selectedMonthEpochDay == month.toEpochDay()
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                role = Role.RadioButton,
                                onClick = { selectedMonthEpochDay = month.toEpochDay() }
                            ),
                        shape = MaterialTheme.shapes.medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedMonthEpochDay = month.toEpochDay()
                                }
                            )
                            Text(
                                text = formatMonthYear(month),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalDate.ofEpochDay(selectedMonthEpochDay).withDayOfMonth(1))
            }) {
                Text(stringResource(R.string.select))
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
private fun AnalyticsComparisonCard(
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
                currentLabel = stringResource(R.string.analytics_current_period),
                previousLabel = stringResource(R.string.analytics_comparison_period),
                currentValue = currentExpenses,
                previousValue = previousExpenses,
                hasPreviousData = hasPreviousData,
                areAmountsHidden = areAmountsHidden,
                accentColor = MaterialTheme.colorScheme.tertiary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
            AnalyticsComparisonRow(
                label = stringResource(R.string.income_in_period),
                currentLabel = stringResource(R.string.analytics_current_period),
                previousLabel = stringResource(R.string.analytics_comparison_period),
                currentValue = currentIncome,
                previousValue = previousIncome,
                hasPreviousData = hasPreviousData,
                areAmountsHidden = areAmountsHidden,
                accentColor = MaterialTheme.colorScheme.secondary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
            AnalyticsComparisonRow(
                label = stringResource(R.string.net_in_period),
                currentLabel = stringResource(R.string.analytics_current_period),
                previousLabel = stringResource(R.string.analytics_comparison_period),
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
    currentLabel: String,
    previousLabel: String,
    currentValue: Long,
    previousValue: Long,
    hasPreviousData: Boolean,
    areAmountsHidden: Boolean,
    accentColor: Color
) {
    val difference = currentValue - previousValue
    val differenceColor = when {
        difference > 0L -> accentColor
        difference < 0L -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
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
                label = currentLabel,
                value = currentValue,
                areAmountsHidden = areAmountsHidden,
                valueColor = MaterialTheme.colorScheme.onSurface
            )
            ComparisonValue(
                modifier = Modifier.weight(1f),
                label = previousLabel,
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
private fun AnalyticsInsightsCard(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsCategoryDetailsSheet(
    categoryName: String,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    areAmountsHidden: Boolean,
    onViewAllTransactions: () -> Unit,
    onDismiss: () -> Unit
) {
    val expenseTotal = transactions.sumOf { it.amountMinor }

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
                            imageVector = Icons.Outlined.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            R.string.analytics_category_transactions_title,
                            categoryName
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (areAmountsHidden) {
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
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (transactions.isEmpty()) {
                Text(
                    text = stringResource(R.string.analytics_no_transactions_category),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsDayDetailsSheet(
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
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            R.string.analytics_day_transactions_title,
                            formatDate(date.toEpochDay())
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (areAmountsHidden) {
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
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (transactions.isEmpty()) {
                Text(
                    text = stringResource(R.string.analytics_no_transactions_day),
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
                    .offset {
                        IntOffset(
                            x = indicatorOffset.roundToPx(),
                            y = 0
                        )
                    }
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
private fun financeSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    checkedBorderColor = MaterialTheme.colorScheme.primary,
    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
    uncheckedBorderColor = MaterialTheme.colorScheme.outline
)

@Composable
private fun SettingsScreen(
    importState: ImportUiState,
    areAmountsHidden: Boolean,
    onAmountsVisibilityChanged: (Boolean) -> Unit,
    onImportFile: (Uri) -> Unit,
    onDismissImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedLanguage = remember {
        currentAppLanguage()
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let(onImportFile) }
    )

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
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.privacy_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.privacy_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = areAmountsHidden,
                    onCheckedChange = onAmountsVisibilityChanged,
                    colors = financeSwitchColors()
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.import_otp_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.import_otp_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.import_otp_formats),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    enabled = !importState.isImporting,
                    onClick = {
                        importLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "text/csv",
                                "text/comma-separated-values",
                                "application/vnd.ms-excel"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (importState.isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (importState.isImporting) {
                            stringResource(R.string.import_otp_importing)
                        } else {
                            stringResource(R.string.import_otp_action)
                        }
                    )
                }
            }
        }
    }

    importState.summary?.let { summary ->
        val formatLabel = when (summary.format) {
            OtpImportFormat.PDF -> stringResource(R.string.import_otp_pdf)
            OtpImportFormat.CSV -> stringResource(R.string.import_otp_csv)
        }
        AlertDialog(
            onDismissRequest = onDismissImport,
            title = { Text(stringResource(R.string.import_otp_success_title)) },
            text = {
                Text(
                    text = if (summary.duplicateCount > 0) {
                        pluralStringResource(
                            R.plurals.import_otp_success_with_duplicates,
                            summary.importedCount,
                            summary.importedCount,
                            summary.parsedCount,
                            formatLabel,
                            summary.duplicateCount
                        )
                    } else {
                        pluralStringResource(
                            R.plurals.import_otp_success,
                            summary.importedCount,
                            summary.importedCount,
                            summary.parsedCount,
                            formatLabel
                        )
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissImport) {
                    Text(stringResource(R.string.import_otp_done))
                }
            }
        )
    }

    importState.error?.let { error ->
        val messageRes = when (error) {
            ImportError.UNSUPPORTED_FILE -> R.string.import_otp_error_unsupported_file
            ImportError.INVALID_FILE -> R.string.import_otp_error_invalid_file
            ImportError.NO_TRANSACTIONS -> R.string.import_otp_error_no_transactions
            ImportError.READ_FAILED -> R.string.import_otp_error_read_failed
        }
        AlertDialog(
            onDismissRequest = onDismissImport,
            title = { Text(stringResource(R.string.import_otp_error_title)) },
            text = { Text(stringResource(messageRes)) },
            confirmButton = {
                TextButton(onClick = onDismissImport) {
                    Text(stringResource(R.string.import_otp_done))
                }
            }
        )
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
    if (category.isSystem) {
        stringResource(categoryLabelRes(category.nameKey))
    } else {
        category.nameKey
    }

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
    "category_online_payments" -> R.string.category_online_payments
    "category_virtual_card" -> R.string.category_virtual_card
    "category_transfers" -> R.string.category_transfers
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

private fun Modifier.amountBlur(areAmountsHidden: Boolean): Modifier =
    if (areAmountsHidden) blur(12.dp, BlurredEdgeTreatment.Unbounded) else this

private class DisplayFormatters(val locale: Locale) {
    val money: NumberFormat = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val date: DateTimeFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(locale)
    val monthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", locale)
}

private val displayFormatters = ThreadLocal<DisplayFormatters>()

private fun currentDisplayFormatters(): DisplayFormatters {
    val locale = Locale.getDefault()
    return displayFormatters.get()?.takeIf { it.locale == locale }
        ?: DisplayFormatters(locale).also(displayFormatters::set)
}

private fun formatMoney(amountMinor: Long): String {
    val formatter = currentDisplayFormatters().money
    return "${formatter.format(BigDecimal.valueOf(amountMinor, 2))} RSD"
}

private fun formatDate(epochDay: Long): String =
    currentDisplayFormatters().date
        .format(LocalDate.ofEpochDay(epochDay))

private fun formatMonthYear(date: LocalDate): String =
    currentDisplayFormatters().monthYear.format(date)

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
