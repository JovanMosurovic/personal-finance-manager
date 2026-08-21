package com.jovanmosurovic.personalfinancemanager.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
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
    val icon: ImageVector
) {
    DASHBOARD("dashboard", R.string.nav_dashboard, Icons.Outlined.Home),
    TRANSACTIONS("transactions", R.string.nav_transactions, Icons.AutoMirrored.Outlined.ReceiptLong),
    ANALYTICS("analytics", R.string.nav_analytics, Icons.Outlined.Analytics),
    CATEGORIES("categories", R.string.nav_categories, Icons.Outlined.Category),
    SETTINGS("settings", R.string.nav_settings, Icons.Outlined.Settings)
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
        bottomBar = {
            if (!showAddTransaction) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = selectedRoute == destination.route,
                            onClick = { selectedRoute = destination.route },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(destination.labelRes)) }
                        )
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
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    uiState: FinanceUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = stringResource(R.string.dashboard_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.balance_total),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatMoney(uiState.totalBalanceMinor),
                    style = MaterialTheme.typography.headlineLarge
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
                value = formatMoney(uiState.incomeThisMonthMinor)
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.expenses_this_month),
                value = formatMoney(uiState.expensesThisMonthMinor)
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.expense_chart_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.expense_chart_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge)
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
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.add_transaction)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.transactions_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            FilterChip(
                selected = showOnlyUncategorized,
                onClick = { showOnlyUncategorized = !showOnlyUncategorized },
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
                Card(modifier = Modifier.fillMaxWidth()) {
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
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
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.categories_title),
            style = MaterialTheme.typography.headlineMedium
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.analytics_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.expense_chart_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.analytics_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.transactions_count, uiState.transactions.size),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
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
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.add_transaction),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedType == TransactionType.EXPENSE,
                onClick = { selectedTypeName = TransactionType.EXPENSE.name },
                label = { Text(stringResource(R.string.add_expense)) }
            )
            FilterChip(
                selected = selectedType == TransactionType.INCOME,
                onClick = { selectedTypeName = TransactionType.INCOME.name },
                label = { Text(stringResource(R.string.add_income)) }
            )
        }

        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
                errorMessageRes = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.amount)) },
            placeholder = { Text(stringResource(R.string.amount_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        OutlinedTextField(
            value = merchant,
            onValueChange = {
                merchant = it
                errorMessageRes = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.merchant)) },
            placeholder = { Text(stringResource(R.string.merchant_hint)) },
            singleLine = true
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.note)) },
            supportingText = { Text(stringResource(R.string.note_optional)) },
            minLines = 2
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = stringResource(R.string.automatic_category_info),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
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
private fun SettingsScreen(modifier: Modifier = Modifier) {
    val selectedLanguage = remember {
        currentAppLanguage()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = stringResource(R.string.language_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.language_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider()
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

@Composable
private fun LanguageOption(
    language: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
