package com.jovanmosurovic.personalfinancemanager.data

import androidx.room.withTransaction
import com.jovanmosurovic.personalfinancemanager.data.local.FinanceDatabase
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
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

    suspend fun addKeyword(categoryId: Long, keyword: String) {
        database.withTransaction {
            addKeywordInternal(categoryId, keyword)
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
            "WHOLE_WORD" -> normalizedMerchant
                .split(Regex("[^A-Z0-9]+"))
                .any { token -> token == normalizedKeyword }

            else -> normalizedMerchant.contains(normalizedKeyword)
        }
    }
}

private fun String.normalizeForMatching(): String = uppercase(Locale.ROOT)
    .replace('Č', 'C')
    .replace('Ć', 'C')
    .replace('Ž', 'Z')
    .replace('Š', 'S')
    .replace('Đ', 'D')
    .replace(Regex("\\s+"), " ")
    .trim()
