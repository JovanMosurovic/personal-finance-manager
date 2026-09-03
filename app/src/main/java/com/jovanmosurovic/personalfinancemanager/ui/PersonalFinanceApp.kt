package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val isBiometricLockEnabled by financeViewModel.isBiometricLockEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val biometricAuthenticator = remember(context) { BiometricAuthenticator(context) }
    var biometricSetupError by rememberSaveable { mutableStateOf(false) }
    val currentRoute by navController.currentBackStackEntryAsState()
    val route = currentRoute?.destination?.route
    val isAddTransactionRoute = route == ADD_TRANSACTION_ROUTE

    var initialCategoryId by rememberSaveable { mutableLongStateOf(ALL_CATEGORIES_FILTER) }
    var initialTypeFilter by rememberSaveable { mutableStateOf(TransactionTypeFilter.ALL) }
    var initialDateEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }

    fun resetTransactionFilters() {
        initialCategoryId = ALL_CATEGORIES_FILTER
        initialTypeFilter = TransactionTypeFilter.ALL
        initialDateEpochDay = null
    }

    fun openTransactions(type: TransactionTypeFilter, categoryId: Long?, dateEpochDay: Long?) {
        initialTypeFilter = type
        initialCategoryId = categoryId ?: ALL_CATEGORIES_FILTER
        initialDateEpochDay = dateEpochDay
        navController.navigate(TopLevelDestination.TRANSACTIONS.route) {
            popUpTo(TopLevelDestination.DASHBOARD.route) { saveState = true }
            launchSingleTop = true
            restoreState = false
        }
    }

    BiometricLock(
        enabled = isBiometricLockEnabled,
        authenticator = biometricAuthenticator
    ) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!isAddTransactionRoute) {
                FinanceBottomNavigation(
                    currentRoute = route,
                    onDestinationSelected = { destination ->
                        if (destination == TopLevelDestination.TRANSACTIONS) {
                            resetTransactionFilters()
                        }
                        navController.navigate(destination.route) {
                            popUpTo(TopLevelDestination.DASHBOARD.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = destination != TopLevelDestination.TRANSACTIONS
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
                        onViewTransactions = { type, date -> openTransactions(type, null, date) }
                    )
                }
                composable(TopLevelDestination.TRANSACTIONS.route) {
                    TransactionsScreen(
                        uiState = uiState,
                        areAmountsHidden = areAmountsHidden,
                        initialCategoryId = initialCategoryId,
                        initialTypeFilter = initialTypeFilter,
                        initialDateEpochDay = initialDateEpochDay,
                        onAddTransaction = { navController.navigate(ADD_TRANSACTION_ROUTE) },
                        onAssignCategory = financeViewModel::assignCategory,
                        onUpdateTransaction = financeViewModel::updateTransaction,
                        onDeleteTransaction = financeViewModel::deleteTransaction
                    )
                }
                composable(TopLevelDestination.ANALYTICS.route) {
                    AnalyticsScreen(
                        uiState = uiState,
                        areAmountsHidden = areAmountsHidden,
                        onViewTransactions = { type, categoryId ->
                            openTransactions(type, categoryId, null)
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
                        isBiometricLockEnabled = isBiometricLockEnabled,
                        biometricSetupError = biometricSetupError,
                        onAmountsVisibilityChanged = financeViewModel::setAmountsHidden,
                        onStatementReminderChanged = financeViewModel::setStatementReminderEnabled,
                        onBiometricLockChanged = { enabled ->
                            if (enabled) {
                                biometricSetupError = !biometricAuthenticator.canAuthenticate()
                                if (!biometricSetupError) {
                                    financeViewModel.setBiometricLockEnabled(true)
                                }
                            } else {
                                biometricSetupError = false
                                financeViewModel.setBiometricLockEnabled(false)
                            }
                        },
                        onImportFile = financeViewModel::importStatement,
                        onDismissImport = financeViewModel::clearImportState
                    )
                }
                composable(ADD_TRANSACTION_ROUTE) {
                    AddTransactionScreen(
                        onCancel = { navController.popBackStack() },
                        onSave = { type, amount, merchant, note, date ->
                            financeViewModel.addTransaction(type, amount, merchant, note, date)
                            navController.popBackStack()
                        }
                    )
                }
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
