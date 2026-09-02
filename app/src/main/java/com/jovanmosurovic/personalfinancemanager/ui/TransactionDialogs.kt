package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionNameFormatter
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType

@Composable
internal fun TransactionDetailsDialog(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    areAmountsHidden: Boolean,
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
        title = { Text(stringResource(R.string.transaction_details)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = TransactionNameFormatter.displayName(transaction.merchant),
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatMoney(if (isIncome) transaction.amountMinor else -transaction.amountMinor),
                    modifier = Modifier.amountBlur(areAmountsHidden),
                    style = MaterialTheme.typography.titleLarge,
                    color = amountColor
                )
                DetailRow(stringResource(R.string.date_label), formatDate(transaction.dateEpochDay))
                DetailRow(
                    stringResource(R.string.category),
                    if (category != null) {
                        categoryLabel(category)
                    } else {
                        stringResource(R.string.no_category_assigned)
                    }
                )
                if (transaction.note.isNotBlank()) {
                    DetailRow(stringResource(R.string.note), transaction.note)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onAssignCategory) {
                        Text(stringResource(R.string.change_category))
                    }
                    TextButton(onClick = onDelete) {
                        Text(stringResource(R.string.delete))
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
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
internal fun EditTransactionDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onSave: (TransactionType, Long, String, String, Long) -> Unit
) {
    var selectedType by rememberSaveable(transaction.id) {
        mutableStateOf(TransactionType.valueOf(transaction.type))
    }
    var amount by rememberSaveable(transaction.id) {
        mutableStateOf(formatEditableAmount(transaction.amountMinor))
    }
    var merchant by rememberSaveable(transaction.id) { mutableStateOf(transaction.merchant) }
    var note by rememberSaveable(transaction.id) { mutableStateOf(transaction.note) }
    var dateEpochDay by rememberSaveable(transaction.id) { mutableLongStateOf(transaction.dateEpochDay) }
    var errorMessage by rememberSaveable(transaction.id) { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_transaction)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TransactionTypeSelector(selectedType, onTypeSelected = { selectedType = it })
                TextField(
                    value = amount,
                    onValueChange = { amount = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                TextField(
                    value = merchant,
                    onValueChange = { merchant = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.merchant)) },
                    singleLine = true
                )
                TransactionDateSelector(dateEpochDay, onDateSelected = { dateEpochDay = it })
                TextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.note)) },
                    minLines = 2
                )
                errorMessage?.let { messageRes ->
                    Text(stringResource(messageRes), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountMinor = parseAmountToMinor(amount)
                    errorMessage = when {
                        amountMinor == null || amountMinor <= 0L -> R.string.invalid_amount
                        merchant.isBlank() -> R.string.merchant_required
                        else -> null
                    }
                    if (errorMessage == null && amountMinor != null) {
                        onSave(selectedType, amountMinor, merchant, note, dateEpochDay)
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
internal fun CategoryAssignmentDialog(
    transaction: TransactionEntity,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onAssign: (Long, Boolean) -> Unit
) {
    var selectedCategoryId by rememberSaveable(transaction.id) {
        mutableStateOf(transaction.categoryId ?: categories.firstOrNull()?.id)
    }
    var rememberKeyword by rememberSaveable(transaction.id) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.assign_category)) },
        text = {
            Column {
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(categories, key = { it.id }) { category ->
                        val parent = category.parentId?.let { parentId ->
                            categories.firstOrNull { it.id == parentId }
                        }
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
                                Text(categoryLabel(category))
                                parent?.let {
                                    Text(
                                        categoryLabel(it),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.remember_keyword),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = rememberKeyword,
                        onCheckedChange = { rememberKeyword = it },
                        colors = financeSwitchColors()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedCategoryId != null,
                onClick = { selectedCategoryId?.let { onAssign(it, rememberKeyword) } }
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
