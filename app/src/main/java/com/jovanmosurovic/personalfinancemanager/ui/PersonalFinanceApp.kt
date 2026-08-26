package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private const val ADD_TRANSACTION_ROUTE = "add_transaction"

@Composable
fun PersonalFinanceApp() {
    val navController = rememberNavController()
    val financeViewModel: FinanceViewModel = hiltViewModel()
    val uiState by financeViewModel.uiState.collectAsStateWithLifecycle()
    val importState by financeViewModel.importState.collectAsStateWithLifecycle()
    val areAmountsHidden by financeViewModel.areAmountsHidden.collectAsStateWithLifecycle()
    val isStatementReminderEnabled by financeViewModel.isStatementReminderEnabled.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val isAddTransactionRoute = currentRoute == ADD_TRANSACTION_ROUTE
    var transactionInitialCategoryId by rememberSaveable {
        mutableStateOf(ALL_CATEGORIES_FILTER)
    }
    var transactionInitialTypeFilter by rememberSaveable {
        mutableStateOf(TransactionTypeFilter.ALL)
    }
    var transactionInitialDateStartEpochDay by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    var transactionInitialDateEndEpochDay by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    val resetTransactionNavigationFilter = {
        transactionInitialCategoryId = ALL_CATEGORIES_FILTER
        transactionInitialTypeFilter = TransactionTypeFilter.ALL
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
        transactionInitialTypeFilter = typeFilter
        transactionInitialDateStartEpochDay = start
        transactionInitialDateEndEpochDay = end
        navController.navigate(TopLevelDestination.TRANSACTIONS.route) {
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
                FinanceBottomNavigation(
                    currentRoute = currentRoute,
                    onDestinationSelected = { destination ->
                        when (destination) {
                            TopLevelDestination.ANALYTICS -> {
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
                            }
                            TopLevelDestination.TRANSACTIONS -> {
                                resetTransactionNavigationFilter()
                                navController.navigate(destination.route) {
                                    popUpTo(TopLevelDestination.DASHBOARD.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }
                            else -> {
                                navController.navigate(destination.route) {
                                    popUpTo(TopLevelDestination.DASHBOARD.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
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
                composable(TopLevelDestination.TRANSACTIONS.route) {
                    TransactionsScreen(
                        uiState = uiState,
                        areAmountsHidden = areAmountsHidden,
                        initialCategoryId = transactionInitialCategoryId,
                        initialTypeFilter = transactionInitialTypeFilter,
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
                        isStatementReminderEnabled = isStatementReminderEnabled,
                        onAmountsVisibilityChanged = financeViewModel::setAmountsHidden,
                        onStatementReminderChanged = financeViewModel::setStatementReminderEnabled,
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
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
