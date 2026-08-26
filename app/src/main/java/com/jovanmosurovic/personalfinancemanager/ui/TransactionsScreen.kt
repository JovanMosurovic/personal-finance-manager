package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionNameFormatter
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType

@Composable
internal fun TransactionsScreen(
    uiState: FinanceUiState,
    areAmountsHidden: Boolean,
    modifier: Modifier = Modifier,
    initialCategoryId: Long = ALL_CATEGORIES_FILTER,
    initialTypeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    initialDateEpochDay: Long? = null,
    onAddTransaction: () -> Unit,
    onAssignCategory: (Long, Long, Boolean) -> Unit,
    onUpdateTransaction: (Long, TransactionType, Long, String, String, Long) -> Unit,
    onDeleteTransaction: (Long) -> Unit
) {
    var searchQuery by rememberSaveable(initialCategoryId, initialTypeFilter, initialDateEpochDay) {
        mutableStateOf("")
    }
    var selectedType by rememberSaveable(initialTypeFilter) { mutableStateOf(initialTypeFilter) }
    var selectedCategoryId by rememberSaveable(initialCategoryId) {
        mutableLongStateOf(initialCategoryId)
    }
    var selectedDateEpochDay by rememberSaveable(initialDateEpochDay) {
        mutableStateOf(initialDateEpochDay)
    }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionBeingEdited by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionForCategory by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionPendingDeletion by remember { mutableStateOf<TransactionEntity?>(null) }

    val categoriesById = remember(uiState.categories) { uiState.categories.associateBy { it.id } }
    val filteredTransactions = remember(
        uiState.transactions,
        searchQuery,
        selectedType,
        selectedCategoryId,
        selectedDateEpochDay
    ) {
        sortTransactions(
            filterTransactions(
                uiState.transactions,
                TransactionFilterCriteria(
                    searchQuery = searchQuery,
                    type = selectedType,
                    categoryId = selectedCategoryId,
                    dateEpochDay = selectedDateEpochDay
                )
            )
        )
    }
    val hasActiveFilters = searchQuery.isNotBlank() ||
        selectedType != TransactionTypeFilter.ALL ||
        selectedCategoryId != ALL_CATEGORIES_FILTER ||
        selectedDateEpochDay != null

    fun clearFilters() {
        searchQuery = ""
        selectedType = TransactionTypeFilter.ALL
        selectedCategoryId = ALL_CATEGORIES_FILTER
        selectedDateEpochDay = null
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_transaction)) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.transactions_title),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_transactions_hint)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.clear_search))
                            }
                        }
                    } else {
                        null
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }
            item {
                TransactionFilterBar(
                    state = TransactionFilterBarState(
                        type = selectedType,
                        categoryId = selectedCategoryId,
                        dateEpochDay = selectedDateEpochDay,
                        hasActiveFilters = hasActiveFilters
                    ),
                    categories = uiState.categories,
                    categoriesById = categoriesById,
                    onTypeSelected = { selectedType = it },
                    onCategorySelected = { selectedCategoryId = it },
                    onClear = ::clearFilters
                )
            }
            item {
                Text(
                    text = stringResource(R.string.transactions_count, filteredTransactions.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (filteredTransactions.isEmpty()) {
                item {
                    EmptyTransactionsCard(hasFilters = hasActiveFilters)
                }
            } else {
                items(filteredTransactions, key = { it.id }) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        category = transaction.categoryId?.let(categoriesById::get),
                        areAmountsHidden = areAmountsHidden,
                        onClick = { selectedTransaction = transaction }
                    )
                }
            }
        }
    }

    selectedTransaction?.let { transaction ->
        TransactionDetailsDialog(
            transaction = transaction,
            category = transaction.categoryId?.let(categoriesById::get),
            areAmountsHidden = areAmountsHidden,
            onDismiss = { selectedTransaction = null },
            onEdit = {
                selectedTransaction = null
                transactionBeingEdited = transaction
            },
            onAssignCategory = {
                selectedTransaction = null
                transactionForCategory = transaction
            },
            onDelete = {
                selectedTransaction = null
                transactionPendingDeletion = transaction
            }
        )
    }

    transactionBeingEdited?.let { transaction ->
        EditTransactionDialog(
            transaction = transaction,
            onDismiss = { transactionBeingEdited = null },
            onSave = { type, amount, merchant, note, date ->
                onUpdateTransaction(transaction.id, type, amount, merchant, note, date)
                transactionBeingEdited = null
            }
        )
    }

    transactionForCategory?.let { transaction ->
        CategoryAssignmentDialog(
            transaction = transaction,
            categories = uiState.categories,
            onDismiss = { transactionForCategory = null },
            onAssign = { categoryId, rememberKeyword ->
                onAssignCategory(transaction.id, categoryId, rememberKeyword)
                transactionForCategory = null
            }
        )
    }

    transactionPendingDeletion?.let { transaction ->
        DeleteConfirmationDialog(
            title = stringResource(R.string.delete_transaction_title),
            message = stringResource(
                R.string.delete_transaction_confirmation,
                TransactionNameFormatter.displayName(transaction.merchant)
            ),
            confirmButtonText = stringResource(R.string.delete_transaction),
            onDismiss = { transactionPendingDeletion = null },
            onConfirm = {
                onDeleteTransaction(transaction.id)
                transactionPendingDeletion = null
            }
        )
    }
}

@Composable
private fun TransactionCard(
    transaction: TransactionEntity,
    category: com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity?,
    areAmountsHidden: Boolean,
    onClick: () -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME.name
    val color = if (isIncome) {
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransactionBadge(transaction, color)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = TransactionNameFormatter.displayName(transaction.merchant),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (category != null) {
                        categoryLabel(category)
                    } else {
                        stringResource(R.string.no_category_assigned)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = formatDate(transaction.dateEpochDay),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatMoney(if (isIncome) transaction.amountMinor else -transaction.amountMinor),
                modifier = Modifier.amountBlur(areAmountsHidden),
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EmptyTransactionsCard(hasFilters: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(
                    if (hasFilters) R.string.no_matching_transactions else R.string.transactions_empty
                ),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(
                    if (hasFilters) {
                        R.string.no_matching_transactions_description
                    } else {
                        R.string.transactions_empty_description
                    }
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
