package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity

internal data class TransactionFilterBarState(
    val type: TransactionTypeFilter,
    val categoryId: Long,
    val date: TransactionDateFilter,
    val dateLabel: String,
    val sort: TransactionSort,
    val uncategorizedCount: Int,
    val hasActiveFilters: Boolean
)

@Composable
internal fun TransactionFilterBar(
    state: TransactionFilterBarState,
    categories: List<CategoryEntity>,
    categoriesById: Map<Long, CategoryEntity>,
    onTypeSelected: (TransactionTypeFilter) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onDateSelected: (TransactionDateFilter) -> Unit,
    onSortSelected: (TransactionSort) -> Unit,
    onClear: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 4.dp)
    ) {
        item {
            TransactionFilterMenuChip(
                label = stringResource(state.type.labelRes),
                icon = Icons.Outlined.FilterList,
                selected = state.type != TransactionTypeFilter.ALL,
                menuContent = { closeMenu ->
                    TransactionTypeFilter.entries.forEach { filter ->
                        DropdownMenuItem(
                            text = { Text(stringResource(filter.labelRes)) },
                            onClick = {
                                onTypeSelected(filter)
                                closeMenu()
                            }
                        )
                    }
                }
            )
        }
        item {
            TransactionFilterMenuChip(
                label = categoryFilterLabel(state, categoriesById),
                icon = Icons.Outlined.Category,
                selected = state.categoryId != ALL_CATEGORIES_FILTER,
                menuContent = { closeMenu ->
                    CategoryFilterMenu(
                        state = state,
                        categories = categories,
                        categoriesById = categoriesById,
                        onCategorySelected = onCategorySelected,
                        closeMenu = closeMenu
                    )
                }
            )
        }
        item {
            TransactionFilterMenuChip(
                label = state.dateLabel,
                icon = Icons.Outlined.DateRange,
                selected = state.date != TransactionDateFilter.ALL_TIME,
                menuContent = { closeMenu ->
                    TransactionDateFilter.entries.forEach { filter ->
                        DropdownMenuItem(
                            text = { Text(stringResource(filter.labelRes)) },
                            onClick = {
                                onDateSelected(filter)
                                closeMenu()
                            }
                        )
                    }
                }
            )
        }
        item {
            TransactionFilterMenuChip(
                label = stringResource(state.sort.labelRes),
                icon = Icons.AutoMirrored.Outlined.Sort,
                selected = state.sort != TransactionSort.NEWEST,
                menuContent = { closeMenu ->
                    TransactionSort.entries.forEach { sort ->
                        DropdownMenuItem(
                            text = { Text(stringResource(sort.labelRes)) },
                            onClick = {
                                onSortSelected(sort)
                                closeMenu()
                            }
                        )
                    }
                }
            )
        }
    }

    if (state.hasActiveFilters) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onClear,
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
}

@Composable
private fun categoryFilterLabel(
    state: TransactionFilterBarState,
    categoriesById: Map<Long, CategoryEntity>
): String {
    return when (state.categoryId) {
        ALL_CATEGORIES_FILTER -> stringResource(R.string.filter_category_all)
        UNCATEGORIZED_FILTER -> stringResource(
            R.string.uncategorized_count,
            state.uncategorizedCount
        )
        else -> categoriesById[state.categoryId]?.let { categoryLabel(it) }
            ?: stringResource(R.string.filter_category_all)
    }
}

@Composable
private fun CategoryFilterMenu(
    state: TransactionFilterBarState,
    categories: List<CategoryEntity>,
    categoriesById: Map<Long, CategoryEntity>,
    onCategorySelected: (Long) -> Unit,
    closeMenu: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.filter_category_all)) },
        onClick = {
            onCategorySelected(ALL_CATEGORIES_FILTER)
            closeMenu()
        }
    )
    DropdownMenuItem(
        text = {
            Text(
                stringResource(
                    R.string.uncategorized_count,
                    state.uncategorizedCount
                )
            )
        },
        onClick = {
            onCategorySelected(UNCATEGORIZED_FILTER)
            closeMenu()
        }
    )
    categories.forEach { category ->
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
                onCategorySelected(category.id)
                closeMenu()
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
