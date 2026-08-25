package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionNameFormatter
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate

@Composable
internal fun TransactionsScreen(
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
        initialTypeFilter,
        initialDateStartEpochDay,
        initialDateEndEpochDay
    ) {
        mutableStateOf("")
    }
    var selectedTypeFilter by rememberSaveable(initialTypeFilter) {
        mutableStateOf(initialTypeFilter)
    }
    var selectedCategoryId by rememberSaveable(initialCategoryId) {
        mutableStateOf(initialCategoryId)
    }
    val hasInitialDateFilter = initialDateStartEpochDay != null && initialDateEndEpochDay != null
    var selectedDateFilter by rememberSaveable(
        initialDateStartEpochDay,
        initialDateEndEpochDay
    ) {
        mutableStateOf(
            if (hasInitialDateFilter) {
                TransactionDateFilter.CUSTOM
            } else {
                TransactionDateFilter.ALL_TIME
            }
        )
    }
    var selectedSort by rememberSaveable(
        initialCategoryId,
        initialTypeFilter,
        initialDateStartEpochDay,
        initialDateEndEpochDay
    ) {
        mutableStateOf(TransactionSort.NEWEST)
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
        initialTypeFilter,
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

    val categoriesById = remember(uiState.categories) {
        uiState.categories.associateBy { it.id }
    }
    val today = LocalDate.now()
    val selectedMonth = LocalDate.ofEpochDay(selectedMonthEpochDay).withDayOfMonth(1)
    val transactionMonths = remember(uiState.transactions, today) {
        availableMonthsUntil(
            transactions = uiState.transactions,
            latestMonth = today.withDayOfMonth(1)
        )
    }
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
            criteria = TransactionFilterCriteria(
                searchQuery = searchQuery,
                type = selectedTypeFilter,
                categoryId = selectedCategoryId,
                date = selectedDateFilter,
                selectedMonthEpochDay = selectedMonthEpochDay,
                customStartEpochDay = customStartEpochDay,
                customEndEpochDay = customEndEpochDay,
                today = today
            )
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
        selectedTypeFilter = TransactionTypeFilter.ALL
        selectedCategoryId = ALL_CATEGORIES_FILTER
        selectedDateFilter = TransactionDateFilter.ALL_TIME
        selectedMonthEpochDay = LocalDate.now().withDayOfMonth(1).toEpochDay()
        customStartEpochDay = null
        customEndEpochDay = null
        showCustomDatePicker = false
        showMonthPicker = false
        selectedSort = TransactionSort.NEWEST
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
                TransactionFilterBar(
                    state = TransactionFilterBarState(
                        type = selectedTypeFilter,
                        categoryId = selectedCategoryId,
                        date = selectedDateFilter,
                        dateLabel = selectedDateFilterLabel,
                        sort = selectedSort,
                        uncategorizedCount = uncategorizedCount,
                        hasActiveFilters = hasActiveFilters
                    ),
                    categories = uiState.categories,
                    categoriesById = categoriesById,
                    onTypeSelected = { selectedTypeFilter = it },
                    onCategorySelected = { selectedCategoryId = it },
                    onDateSelected = { filter ->
                        when (filter) {
                            TransactionDateFilter.MONTH -> showMonthPicker = true
                            TransactionDateFilter.CUSTOM -> showCustomDatePicker = true
                            else -> selectedDateFilter = filter
                        }
                    },
                    onSortSelected = { selectedSort = it },
                    onClear = clearFilters
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (visibleTransactions.isEmpty() && uiState.transactions.isNotEmpty()) {
                    NoMatchingTransactionsContent(
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

            BottomFadeOverlay(
                modifier = Modifier.align(Alignment.BottomCenter)
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
        DeleteConfirmationDialog(
            title = stringResource(R.string.delete_transaction_title),
            message = stringResource(
                R.string.delete_transaction_confirmation,
                transaction.merchant
            ),
            confirmButtonText = stringResource(R.string.delete_transaction),
            onDismiss = { transactionPendingDeletion = null },
            onConfirm = {
                onDeleteTransaction(transaction.id)
                transactionPendingDeletion = null
            }
        )
    }

    if (showCustomDatePicker) {
        DateRangePickerDialog(
            initialStartEpochDay = customStartEpochDay,
            initialEndEpochDay = customEndEpochDay,
            onDismiss = { showCustomDatePicker = false },
            onConfirm = { startEpochDay, endEpochDay ->
                customStartEpochDay = minOf(startEpochDay, endEpochDay)
                customEndEpochDay = maxOf(startEpochDay, endEpochDay)
                selectedDateFilter = TransactionDateFilter.CUSTOM
                showCustomDatePicker = false
            }
        )
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            titleRes = R.string.select_transaction_month,
            initialMonth = selectedMonth,
            availableMonths = transactionMonths,
            onDismiss = { showMonthPicker = false },
            onConfirm = { month ->
                selectedMonthEpochDay = month.withDayOfMonth(1).toEpochDay()
                selectedDateFilter = TransactionDateFilter.MONTH
                showMonthPicker = false
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
private fun NoMatchingTransactionsContent(
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
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.clear_filters))
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
