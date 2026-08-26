package com.jovanmosurovic.personalfinancemanager.data

import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultFinanceDataTest {
    @Test
    fun containsLiteCategoryTree() {
        val groceries = DefaultFinanceData.categories.first { it.id == DefaultFinanceData.GROCERIES }
        val salary = DefaultFinanceData.categories.first { it.id == DefaultFinanceData.SALARY }

        assertEquals(DefaultFinanceData.FOOD, groceries.parentId)
        assertEquals(DefaultFinanceData.INCOME, salary.parentId)
        assertTrue(DefaultFinanceData.categories.any { it.id == DefaultFinanceData.BILLS })
        assertTrue(DefaultFinanceData.categories.any { it.id == DefaultFinanceData.TRANSPORT })
    }

    @Test
    fun containsRepresentativeOtpRulesForExpenseAndIncome() {
        assertTrue(
            DefaultFinanceData.keywordRules.any { rule ->
                rule.categoryId == DefaultFinanceData.GROCERIES &&
                    rule.keyword == "MAXI" &&
                    rule.transactionType == TransactionType.EXPENSE.name
            }
        )
        assertTrue(
            DefaultFinanceData.keywordRules.any { rule ->
                rule.categoryId == DefaultFinanceData.DELIVERY &&
                    rule.keyword == "GLOVO" &&
                    rule.transactionType == TransactionType.EXPENSE.name
            }
        )
        assertTrue(
            DefaultFinanceData.keywordRules.any { rule ->
                rule.categoryId == DefaultFinanceData.SALARY &&
                    rule.keyword == "JETBRAINS" &&
                    rule.transactionType == TransactionType.INCOME.name
            }
        )
    }
}
