package com.jovanmosurovic.personalfinancemanager.data

import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import com.jovanmosurovic.personalfinancemanager.domain.model.KnownOtpAccounts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultFinanceDataTest {
    @Test
    fun includesVirtualCardAsExpenseCategory() {
        val virtualCard = DefaultFinanceData.categories.first { it.id == DefaultFinanceData.VIRTUAL_CARD }

        assertEquals(DefaultFinanceData.ONLINE_PAYMENTS, virtualCard.parentId)
        assertTrue(
            DefaultFinanceData.keywordRules.any { rule ->
                rule.categoryId == DefaultFinanceData.VIRTUAL_CARD &&
                    rule.keyword ==
                        "PRENOS U KORIST ${KnownOtpAccounts.LEGACY_VIRTUAL_CARD_ACCOUNT}" &&
                    rule.transactionType == TransactionType.EXPENSE.name &&
                    rule.matchMode == "EXACT"
            }
        )
        assertTrue(
            DefaultFinanceData.keywordRules.any { rule ->
                rule.categoryId == DefaultFinanceData.VIRTUAL_CARD &&
                    rule.keyword ==
                        "INTERNI PRENOS SA RACUNA ${KnownOtpAccounts.CURRENT_ACCOUNT} " +
                        "NA RACUN ${KnownOtpAccounts.VIRTUAL_CARD_ACCOUNT}" &&
                    rule.matchMode == "EXACT"
            }
        )

        val transfers = DefaultFinanceData.categories.first { it.id == DefaultFinanceData.TRANSFERS }
        assertEquals(null, transfers.parentId)
        assertTrue(
            DefaultFinanceData.keywordRules.count { rule ->
                rule.categoryId == DefaultFinanceData.TRANSFERS &&
                    rule.transactionType == "ANY" &&
                    rule.matchMode == "CONTAINS"
            } == 5
        )
        assertTrue(
            DefaultFinanceData.keywordRules.any { rule ->
                rule.categoryId == DefaultFinanceData.TRANSFERS &&
                    rule.keyword == KnownOtpAccounts.VIOLETA_DAMNJANOVIC_ACCOUNT
            }
        )
    }
}
