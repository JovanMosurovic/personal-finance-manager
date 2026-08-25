package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.KeyboardType
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
                    onTypeSelected = { selectedType = it }
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
internal fun CategoryAssignmentDialog(
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
                                    onSelect = { selectedCategoryId = category.id },
                                    onExpand = if (children.isNotEmpty()) {
                                        { expandedParentCategoryId = category.id }
                                    } else {
                                        null
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
    onSelect: () -> Unit,
    onExpand: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpand ?: onSelect),
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
            if (onExpand != null) {
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
