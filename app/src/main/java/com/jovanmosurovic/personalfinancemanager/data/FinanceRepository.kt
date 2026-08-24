package com.jovanmosurovic.personalfinancemanager.data

import androidx.room.withTransaction
import com.jovanmosurovic.personalfinancemanager.data.local.FinanceDatabase
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.data.importer.OtpParsedTransaction
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class FinanceRepository(
    private val database: FinanceDatabase
) {
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
        val normalizedMerchant = merchant.normalizeForMatching()
        val matchedRule = database.keywordRuleDao()
            .getActiveRules()
            .firstOrNull { rule ->
                ruleAppliesToTransaction(rule, type) &&
                    keywordMatches(rule, normalizedMerchant)
            }

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
                database.keywordRuleDao()
                    .getActiveRules()
                    .firstOrNull { rule ->
                        ruleAppliesToTransaction(rule, type) &&
                            keywordMatches(rule, cleanedMerchant.normalizeForMatching())
                    }
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

                val matchedRule = rules.firstOrNull { rule ->
                    ruleAppliesToTransaction(rule, importedTransaction.type) &&
                        keywordMatches(rule, importedTransaction.merchant.normalizeForMatching())
                }
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

            val alreadyExists = categoryDao.getAll().any { category ->
                !category.isSystem &&
                    category.parentId == parentId &&
                    category.nameKey.equals(cleanedName, ignoreCase = true)
            }
            if (alreadyExists) return@withTransaction

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

            val alreadyExists = categoryDao.getAll().any { otherCategory ->
                otherCategory.id != categoryId &&
                    !otherCategory.isSystem &&
                    otherCategory.parentId == category.parentId &&
                    otherCategory.nameKey.equals(cleanedName, ignoreCase = true)
            }
            if (alreadyExists) return@withTransaction

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

    private suspend fun reclassifyUncategorizedInternal() {
        val rules = database.keywordRuleDao().getActiveRules()
        database.transactionDao().getUncategorized().forEach { transaction ->
            val matchedRule = rules.firstOrNull { rule ->
                ruleAppliesToTransaction(rule, TransactionType.valueOf(transaction.type)) &&
                    keywordMatches(rule, transaction.merchant.normalizeForMatching())
            }
            if (matchedRule != null) {
                database.transactionDao().applyAutomaticCategory(
                    transactionId = transaction.id,
                    categoryId = matchedRule.categoryId,
                    ruleId = matchedRule.id
                )
            }
        }
    }

    private fun ruleAppliesToTransaction(
        rule: KeywordRuleEntity,
        transactionType: TransactionType
    ): Boolean = rule.transactionType == "ANY" || rule.transactionType == transactionType.name

    private fun keywordMatches(
        rule: KeywordRuleEntity,
        normalizedMerchant: String
    ): Boolean {
        val normalizedKeyword = rule.keyword.normalizeForMatching()
        return when (rule.matchMode) {
            "EXACT" -> normalizedMerchant == normalizedKeyword

            "WHOLE_WORD" -> normalizedMerchant
                .split(Regex("[^A-Z0-9]+"))
                .any { token -> token == normalizedKeyword }

            else -> normalizedMerchant.contains(normalizedKeyword)
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
    merchant.normalizeForMatching(),
    note.normalizeForMatching()
).joinToString("|")

private fun String.normalizeForMatching(): String = uppercase(Locale.ROOT)
    .replace('Č', 'C')
    .replace('Ć', 'C')
    .replace('Ž', 'Z')
    .replace('Š', 'S')
    .replace('Đ', 'D')
    .replace(Regex("\\s+"), " ")
    .trim()
    .replace(
        Regex("\\s+(?:DATUM I VREME STAMPE|WWW\\.OTPBANKA\\.RS|VASA OTP BANKA|[123] OD [123]).*"),
        ""
    )
    .replace(Regex("\\s+\\d{1,3}(?:[.]\\d{3})+[,]\\d{2}$"), "")
    .trim()
