package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity

internal data class TransactionFilterBarState(
    val type: TransactionTypeFilter,
    val categoryId: Long,
    val dateEpochDay: Long?,
    val hasActiveFilters: Boolean
)

@Composable
internal fun TransactionFilterBar(
    state: TransactionFilterBarState,
    categories: List<CategoryEntity>,
    categoriesById: Map<Long, CategoryEntity>,
    onTypeSelected: (TransactionTypeFilter) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onClear: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 4.dp)
    ) {
        item {
            TypeFilterChip(
                selectedType = state.type,
                onTypeSelected = onTypeSelected
            )
        }
        item {
            CategoryFilterChip(
                state = state,
                categories = categories,
                categoriesById = categoriesById,
                onCategorySelected = onCategorySelected
            )
        }
        if (state.dateEpochDay != null) {
            item {
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text(formatDate(state.dateEpochDay)) },
                    leadingIcon = { Icon(Icons.Outlined.Today, null) }
                )
            }
        }
    }
    if (state.hasActiveFilters) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(
                onClick = onClear,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Outlined.Close, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.clear_filters))
            }
        }
    }
}

@Composable
private fun CategoryFilterChip(
    state: TransactionFilterBarState,
    categories: List<CategoryEntity>,
    categoriesById: Map<Long, CategoryEntity>,
    onCategorySelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = state.categoryId != ALL_CATEGORIES_FILTER,
            onClick = { expanded = true },
            label = {
                Text(
                    if (categoriesById[state.categoryId] != null) {
                        categoryLabel(categoriesById.getValue(state.categoryId))
                    } else {
                        stringResource(R.string.filter_category_all)
                    }
                )
            },
            leadingIcon = { Icon(Icons.Outlined.Category, null) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_category_all)) },
                onClick = {
                    onCategorySelected(ALL_CATEGORIES_FILTER)
                    expanded = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(categoryLabel(category)) },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TypeFilterChip(
    selectedType: TransactionTypeFilter,
    onTypeSelected: (TransactionTypeFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = selectedType != TransactionTypeFilter.ALL,
            onClick = { expanded = true },
            label = { Text(stringResource(selectedType.labelRes)) },
            leadingIcon = { Icon(Icons.Outlined.FilterList, null) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TransactionTypeFilter.entries.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(stringResource(filter.labelRes)) },
                    onClick = {
                        onTypeSelected(filter)
                        expanded = false
                    }
                )
            }
        }
    }
}
