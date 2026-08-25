package com.jovanmosurovic.personalfinancemanager.data

import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.util.Locale

class TransactionRuleMatcher {
    fun findMatchingRule(
        rules: List<KeywordRuleEntity>,
        transactionType: TransactionType,
        merchant: String
    ): KeywordRuleEntity? {
        val normalizedMerchant = normalizeForMatching(merchant)

        return rules.firstOrNull { rule ->
            ruleAppliesToTransaction(rule, transactionType) &&
                keywordMatches(rule, normalizedMerchant)
        }
    }

    private fun ruleAppliesToTransaction(
        rule: KeywordRuleEntity,
        transactionType: TransactionType
    ): Boolean {
        return rule.transactionType == "ANY" ||
            rule.transactionType == transactionType.name
    }

    private fun keywordMatches(
        rule: KeywordRuleEntity,
        normalizedMerchant: String
    ): Boolean {
        val normalizedKeyword = normalizeForMatching(rule.keyword)

        return when (rule.matchMode) {
            "EXACT" -> normalizedMerchant == normalizedKeyword

            "WHOLE_WORD" -> normalizedMerchant
                .split(Regex("[^A-Z0-9]+"))
                .any { token -> token == normalizedKeyword }

            else -> normalizedMerchant.contains(normalizedKeyword)
        }
    }
}

internal fun normalizeForMatching(value: String): String = value
    .uppercase(Locale.ROOT)
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
