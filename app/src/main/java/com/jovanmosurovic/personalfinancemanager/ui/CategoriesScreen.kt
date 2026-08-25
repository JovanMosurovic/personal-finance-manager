package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity

private data class CategoryEditorState(
    val category: CategoryEntity? = null,
    val parentCategory: CategoryEntity? = null
)

@Composable
internal fun CategoriesScreen(
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

        BottomFadeOverlay(
            modifier = Modifier.align(Alignment.BottomCenter)
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
        DeleteConfirmationDialog(
            title = stringResource(R.string.delete_keyword_title),
            message = stringResource(
                R.string.delete_keyword_confirmation,
                rule.keyword
            ),
            confirmButtonText = stringResource(R.string.delete_keyword),
            onDismiss = { keywordPendingDeletion = null },
            onConfirm = {
                onDeleteKeyword(rule.id)
                keywordPendingDeletion = null
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
        DeleteConfirmationDialog(
            title = stringResource(R.string.delete_category_title),
            message = stringResource(
                R.string.delete_category_confirmation,
                categoryLabel(category)
            ),
            confirmButtonText = stringResource(R.string.delete_category),
            onDismiss = { categoryPendingDeletion = null },
            onConfirm = {
                onDeleteCategory(category.id)
                categoryPendingDeletion = null
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
