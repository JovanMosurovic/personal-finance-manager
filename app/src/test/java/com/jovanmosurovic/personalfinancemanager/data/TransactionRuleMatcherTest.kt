package com.jovanmosurovic.personalfinancemanager.data

import com.jovanmosurovic.personalfinancemanager.data.local.entity.KeywordRuleEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionRuleMatcherTest {
    private val matcher = TransactionRuleMatcher()

    @Test
    fun exactRuleMatchesOnlyTheWholeNormalizedDescription() {
        val rule = rule(
            keyword = "PRENOS U KORIST 9120726623676",
            matchMode = "EXACT"
        )

        assertEquals(
            rule,
            matcher.findMatchingRule(
                rules = listOf(rule),
                transactionType = TransactionType.EXPENSE,
                merchant = "  Prenos u korist 9120726623676  "
            )
        )
        assertNull(
            matcher.findMatchingRule(
                rules = listOf(rule),
                transactionType = TransactionType.EXPENSE,
                merchant = "m-Banking prenos u korist 9120726623676"
            )
        )
    }

    @Test
    fun normalizesSerbianCharactersAndPdfFooterBeforeMatching() {
        val rule = rule(
            keyword = "INTERNI PRENOS SA RACUNA 325930070633456009 " +
                "NA RACUN 325912072662367691",
            matchMode = "EXACT"
        )

        assertEquals(
            rule,
            matcher.findMatchingRule(
                rules = listOf(rule),
                transactionType = TransactionType.EXPENSE,
                merchant = "Interni prenos sa računa 325930070633456009 " +
                    "na račun 325912072662367691 444.636,05 " +
                    "Datum i vreme štampe: 07.08.2026."
            )
        )
    }

    @Test
    fun anyRuleMatchesIncomeAndExpenseTransactions() {
        val rule = rule(
            keyword = "9300500124019",
            transactionType = "ANY"
        )

        assertEquals(
            rule,
            matcher.findMatchingRule(
                rules = listOf(rule),
                transactionType = TransactionType.INCOME,
                merchant = "Priliv sa racuna 9300500124019"
            )
        )
        assertEquals(
            rule,
            matcher.findMatchingRule(
                rules = listOf(rule),
                transactionType = TransactionType.EXPENSE,
                merchant = "Prenos u korist 9300500124019"
            )
        )
    }

    @Test
    fun typedRuleDoesNotMatchTheOtherTransactionType() {
        val rule = rule(
            keyword = "JETBRAINS",
            transactionType = TransactionType.INCOME.name
        )

        assertNull(
            matcher.findMatchingRule(
                rules = listOf(rule),
                transactionType = TransactionType.EXPENSE,
                merchant = "JETBRAINS D.O.O."
            )
        )
    }

    @Test
    fun wholeWordRuleDoesNotMatchPartOfAnotherWord() {
        val rule = rule(
            keyword = "DM",
            matchMode = "WHOLE_WORD"
        )

        assertEquals(
            rule,
            matcher.findMatchingRule(
                rules = listOf(rule),
                transactionType = TransactionType.EXPENSE,
                merchant = "Kupovina u DM, Beograd"
            )
        )
        assertNull(
            matcher.findMatchingRule(
                rules = listOf(rule),
                transactionType = TransactionType.EXPENSE,
                merchant = "DMARKET"
            )
        )
    }

    @Test
    fun returnsTheFirstMatchingRuleFromTheGivenOrder() {
        val firstRule = rule(keyword = "GLOVO", categoryId = 1)
        val secondRule = rule(keyword = "GLOVOAPP", categoryId = 2)

        assertEquals(
            firstRule,
            matcher.findMatchingRule(
                rules = listOf(firstRule, secondRule),
                transactionType = TransactionType.EXPENSE,
                merchant = "GLOVOAPP BEOGRAD"
            )
        )
    }

    private fun rule(
        keyword: String,
        categoryId: Long = 1,
        transactionType: String = TransactionType.EXPENSE.name,
        matchMode: String = "CONTAINS"
    ): KeywordRuleEntity {
        return KeywordRuleEntity(
            name = "Test rule",
            keyword = keyword,
            categoryId = categoryId,
            transactionType = transactionType,
            matchMode = matchMode
        )
    }
}
