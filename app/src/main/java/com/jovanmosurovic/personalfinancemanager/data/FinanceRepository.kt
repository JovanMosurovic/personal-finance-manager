package com.jovanmosurovic.personalfinancemanager.data

import androidx.room.withTransaction
import com.jovanmosurovic.personalfinancemanager.data.local.FinanceDatabase
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.data.importer.OtpParsedTransaction
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val database: FinanceDatabase
) {
    private val ruleMatcher = TransactionRuleMatcher()

    fun observeTransactions(): Flow<List<TransactionEntity>> =
        database.transactionDao().observeAll()

    fun observeCategories(): Flow<List<CategoryEntity>> =
        database.categoryDao().observeAll()

    fun observeKeywordRules(): Flow<List<KeywordRuleEntity>> =
        database.keywordRuleDao().observeAll()

    suspend fun seedDefaults() {
        database.withTransaction {
            if (database.categoryDao().count() == 0) {
                database.categoryDao().insertAll(DefaultFinanceData.categories)
            }
            if (database.keywordRuleDao().count() == 0) {
                database.keywordRuleDao().insertAll(DefaultFinanceData.keywordRules)
            }
            reclassifyUncategorizedInternal()
        }
    }

    suspend fun addTransaction(
        type: TransactionType,
        amountMinor: Long,
        merchant: String,
        note: String,
        dateEpochDay: Long
    ) {
        val matchedRule = ruleMatcher.findMatchingRule(
            rules = database.keywordRuleDao().getActiveRules(),
            transactionType = type,
            merchant = merchant
        )

        database.transactionDao().insert(
            TransactionEntity(
                type = type.name,
                amountMinor = amountMinor,
                merchant = merchant.trim(),
                note = note.trim(),
                dateEpochDay = dateEpochDay,
                categoryId = matchedRule?.categoryId,
                matchedRuleId = matchedRule?.id,
                isManuallyCategorized = false
            )
        )
    }

    suspend fun updateTransaction(
        transactionId: Long,
        type: TransactionType,
        amountMinor: Long,
        merchant: String,
        note: String,
        dateEpochDay: Long
    ) {
        database.withTransaction {
            val existingTransaction = database.transactionDao().getById(transactionId)
                ?: return@withTransaction
            val cleanedMerchant = merchant.trim()
            val matchedRule = if (existingTransaction.isManuallyCategorized) {
                null
            } else {
                ruleMatcher.findMatchingRule(
                    rules = database.keywordRuleDao().getActiveRules(),
                    transactionType = type,
                    merchant = cleanedMerchant
                )
            }

            database.transactionDao().update(
                existingTransaction.copy(
                    type = type.name,
                    amountMinor = amountMinor,
                    merchant = cleanedMerchant,
                    note = note.trim(),
                    dateEpochDay = dateEpochDay,
                    categoryId = if (existingTransaction.isManuallyCategorized) {
                        existingTransaction.categoryId
                    } else {
                        matchedRule?.categoryId
                    },
                    matchedRuleId = if (existingTransaction.isManuallyCategorized) {
                        null
                    } else {
                        matchedRule?.id
                    }
                )
            )
        }
    }

    suspend fun deleteTransaction(transactionId: Long) {
        database.transactionDao().deleteById(transactionId)
    }

    suspend fun importTransactions(transactions: List<OtpParsedTransaction>): ImportInsertResult {
        var importedCount = 0
        var duplicateCount = 0

        database.withTransaction {
            val transactionDao = database.transactionDao()
            val existingKeys = transactionDao.getAll()
                .mapTo(mutableSetOf()) { it.importKey() }
            val rules = database.keywordRuleDao().getActiveRules()

            transactions.forEach { importedTransaction ->
                val transaction = TransactionEntity(
                    type = importedTransaction.type.name,
                    amountMinor = importedTransaction.amountMinor,
                    merchant = importedTransaction.merchant,
                    note = importedTransaction.note,
                    dateEpochDay = importedTransaction.dateEpochDay,
                    categoryId = null,
                    matchedRuleId = null,
                    isManuallyCategorized = false
                )

                if (!existingKeys.add(transaction.importKey())) {
                    duplicateCount++
                    return@forEach
                }

                val matchedRule = ruleMatcher.findMatchingRule(
                    rules = rules,
                    transactionType = importedTransaction.type,
                    merchant = importedTransaction.merchant
                )
                transactionDao.insert(
                    transaction.copy(
                        categoryId = matchedRule?.categoryId,
                        matchedRuleId = matchedRule?.id
                    )
                )
                importedCount++
            }
        }

        return ImportInsertResult(
            importedCount = importedCount,
            duplicateCount = duplicateCount
        )
    }

    suspend fun addCategory(name: String, parentId: Long?) {
        val cleanedName = name.trim()
        if (cleanedName.isBlank()) return

        database.withTransaction {
            val categoryDao = database.categoryDao()
            val parent = parentId?.let { categoryDao.getById(it) }
            if (parentId != null && (parent == null || parent.parentId != null)) {
                return@withTransaction
            }

            if (customCategoryExists(cleanedName, parentId)) return@withTransaction

            categoryDao.insert(
                CategoryEntity(
                    id = categoryDao.nextId(),
                    nameKey = cleanedName,
                    parentId = parentId,
                    isSystem = false
                )
            )
        }
    }

    suspend fun renameCategory(categoryId: Long, name: String) {
        val cleanedName = name.trim()
        if (cleanedName.isBlank()) return

        database.withTransaction {
            val categoryDao = database.categoryDao()
            val category = categoryDao.getById(categoryId)
                ?: return@withTransaction
            if (category.isSystem) return@withTransaction

            val duplicateExists = customCategoryExists(
                name = cleanedName,
                parentId = category.parentId,
                ignoredCategoryId = categoryId
            )
            if (duplicateExists) return@withTransaction

            categoryDao.renameCustomCategory(categoryId, cleanedName)
        }
    }

    suspend fun deleteCategory(categoryId: Long) {
        database.withTransaction {
            val categoryDao = database.categoryDao()
            val category = categoryDao.getById(categoryId)
                ?: return@withTransaction
            if (category.isSystem) return@withTransaction

            suspend fun deleteCategoryTree(currentCategoryId: Long) {
                categoryDao.getChildren(currentCategoryId).forEach { child ->
                    deleteCategoryTree(child.id)
                }
                database.transactionDao().clearCategoryAssignments(currentCategoryId)
                categoryDao.deleteById(currentCategoryId)
            }

            deleteCategoryTree(categoryId)
            reclassifyUncategorizedInternal()
        }
    }

    suspend fun addKeyword(categoryId: Long, keyword: String) {
        database.withTransaction {
            addKeywordInternal(categoryId, keyword)
            reclassifyUncategorizedInternal()
        }
    }

    suspend fun deleteKeyword(keywordRuleId: Long) {
        database.withTransaction {
            database.transactionDao().clearAutomaticCategoryForRule(keywordRuleId)
            database.keywordRuleDao().deleteById(keywordRuleId)
            reclassifyUncategorizedInternal()
        }
    }

    suspend fun assignCategory(
        transactionId: Long,
        categoryId: Long,
        saveMerchantAsKeyword: Boolean
    ) {
        database.withTransaction {
            val transaction = database.transactionDao().getById(transactionId)
                ?: return@withTransaction

            database.transactionDao().assignCategory(transactionId, categoryId)
            if (saveMerchantAsKeyword) {
                addKeywordInternal(categoryId, transaction.merchant)
                reclassifyUncategorizedInternal()
            }
        }
    }

    private suspend fun addKeywordInternal(categoryId: Long, keyword: String) {
        val cleanedKeyword = keyword.trim()
        if (cleanedKeyword.isBlank()) return

        val alreadyExists = database.keywordRuleDao()
            .getForCategory(categoryId)
            .any { it.keyword.equals(cleanedKeyword, ignoreCase = true) }
        if (alreadyExists) return

        database.keywordRuleDao().insert(
            KeywordRuleEntity(
                name = "Custom keyword",
                keyword = cleanedKeyword,
                categoryId = categoryId,
                priority = 100,
                matchMode = if (cleanedKeyword.length <= 3) "WHOLE_WORD" else "CONTAINS"
            )
        )
    }

    private suspend fun customCategoryExists(
        name: String,
        parentId: Long?,
        ignoredCategoryId: Long? = null
    ): Boolean {
        return database.categoryDao().getAll().any { category ->
            category.id != ignoredCategoryId &&
                !category.isSystem &&
                category.parentId == parentId &&
                category.nameKey.equals(name, ignoreCase = true)
        }
    }

    private suspend fun reclassifyUncategorizedInternal() {
        val rules = database.keywordRuleDao().getActiveRules()
        database.transactionDao().getUncategorized().forEach { transaction ->
            val matchedRule = ruleMatcher.findMatchingRule(
                rules = rules,
                transactionType = TransactionType.valueOf(transaction.type),
                merchant = transaction.merchant
            )
            if (matchedRule != null) {
                database.transactionDao().applyAutomaticCategory(
                    transactionId = transaction.id,
                    categoryId = matchedRule.categoryId,
                    ruleId = matchedRule.id
                )
            }
        }
    }
}

data class ImportInsertResult(
    val importedCount: Int,
    val duplicateCount: Int
)

private fun TransactionEntity.importKey(): String = listOf(
    type,
    amountMinor,
    dateEpochDay,
    normalizeForMatching(merchant),
    normalizeForMatching(note)
).joinToString("|")
