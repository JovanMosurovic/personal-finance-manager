package com.jovanmosurovic.personalfinancemanager.ui

import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionFilteringTest {
    private val today = LocalDate.of(2026, 8, 25)

    @Test
    fun searchMatchesMerchantAndNote() {
        val transactions = listOf(
            transaction(id = 1, merchant = "MOL SERBIA 1234 BEOGRAD"),
            transaction(id = 2, merchant = "LOCAL SHOP", note = "School supplies"),
            transaction(id = 3, merchant = "Bookstore")
        )

        assertEquals(listOf(1L), filter(transactions, searchQuery = "  mol ").map { it.id })
        assertEquals(listOf(2L), filter(transactions, searchQuery = "SCHOOL").map { it.id })
        assertEquals(listOf(3L), filter(transactions, searchQuery = "book").map { it.id })
    }

    @Test
    fun typeAndCategoryFiltersSelectOnlyMatchingTransactions() {
        val transactions = listOf(
            transaction(id = 1, type = TransactionType.INCOME, categoryId = 10),
            transaction(id = 2, type = TransactionType.EXPENSE, categoryId = 10),
            transaction(id = 3, type = TransactionType.EXPENSE, categoryId = null)
        )

        assertEquals(
            listOf(1L),
            filter(transactions, typeFilter = TransactionTypeFilter.INCOME).map { it.id }
        )
        assertEquals(
            listOf(2L),
            filter(
                transactions,
                typeFilter = TransactionTypeFilter.EXPENSE,
                categoryId = 10
            ).map { it.id }
        )
        assertEquals(
            listOf(1L, 2L, 3L),
            filter(transactions, categoryId = ALL_CATEGORIES_FILTER).map { it.id }
        )
    }

    @Test
    fun dateFilterMatchesOnlyTheSelectedDay() {
        val transactions = listOf(
            transaction(id = 1, date = today),
            transaction(id = 2, date = today.minusDays(1)),
            transaction(id = 3, date = today.plusDays(1))
        )

        assertEquals(
            listOf(1L),
            filter(transactions, dateEpochDay = today.toEpochDay()).map { it.id }
        )
    }

    @Test
    fun sortingUsesNewestDateAndIdAsTieBreaker() {
        val transactions = listOf(
            transaction(id = 1, date = today.minusDays(1)),
            transaction(id = 2, date = today),
            transaction(id = 3, date = today)
        )

        assertEquals(
            listOf(3L, 2L, 1L),
            sortTransactions(transactions).map { it.id }
        )
    }

    private fun filter(
        transactions: List<TransactionEntity>,
        searchQuery: String = "",
        typeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
        categoryId: Long = ALL_CATEGORIES_FILTER,
        dateEpochDay: Long? = null
    ): List<TransactionEntity> = filterTransactions(
        transactions = transactions,
        criteria = TransactionFilterCriteria(
            searchQuery = searchQuery,
            type = typeFilter,
            categoryId = categoryId,
            dateEpochDay = dateEpochDay
        )
    )

    private fun transaction(
        id: Long,
        date: LocalDate = today,
        type: TransactionType = TransactionType.EXPENSE,
        amountMinor: Long = 100,
        merchant: String = "Merchant $id",
        note: String = "",
        categoryId: Long? = null
    ) = TransactionEntity(
        id = id,
        type = type.name,
        amountMinor = amountMinor,
        merchant = merchant,
        note = note,
        dateEpochDay = date.toEpochDay(),
        categoryId = categoryId
    )
}
