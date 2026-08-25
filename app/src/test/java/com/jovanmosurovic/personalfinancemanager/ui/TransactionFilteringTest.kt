package com.jovanmosurovic.personalfinancemanager.ui

import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionFilteringTest {
    private val today = LocalDate.of(2026, 8, 25)

    @Test
    fun searchMatchesDisplayNameMerchantAndNote() {
        val transactions = listOf(
            transaction(
                id = 1,
                merchant = "MOL SERBIA 1234 BEOGRAD"
            ),
            transaction(
                id = 2,
                merchant = "LOCAL SHOP",
                note = "School supplies"
            ),
            transaction(
                id = 3,
                merchant = "Bookstore"
            )
        )

        assertEquals(listOf(1L), filter(transactions, searchQuery = "  gorivo ").map { it.id })
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
            listOf(3L),
            filter(transactions, categoryId = UNCATEGORIZED_FILTER).map { it.id }
        )
        assertEquals(
            listOf(1L, 2L, 3L),
            filter(transactions, categoryId = ALL_CATEGORIES_FILTER).map { it.id }
        )
    }

    @Test
    fun relativeDateFiltersIncludeTheirBoundaryDaysAndExcludeFutureDates() {
        val transactions = listOf(
            transaction(id = 1, date = today),
            transaction(id = 2, date = today.minusDays(6)),
            transaction(id = 3, date = today.minusDays(7)),
            transaction(id = 4, date = today.minusDays(29)),
            transaction(id = 5, date = today.minusDays(30)),
            transaction(id = 6, date = today.withDayOfMonth(1)),
            transaction(id = 7, date = today.withDayOfMonth(1).minusDays(1)),
            transaction(id = 8, date = today.plusDays(1))
        )

        assertEquals(
            listOf(1L, 2L),
            filter(transactions, dateFilter = TransactionDateFilter.LAST_7_DAYS).map { it.id }
        )
        assertEquals(
            listOf(1L, 2L, 3L, 4L, 6L, 7L),
            filter(transactions, dateFilter = TransactionDateFilter.LAST_30_DAYS).map { it.id }
        )
        assertEquals(
            listOf(1L, 2L, 3L, 6L),
            filter(transactions, dateFilter = TransactionDateFilter.THIS_MONTH).map { it.id }
        )
    }

    @Test
    fun monthAndCustomDateFiltersAreInclusive() {
        val july = LocalDate.of(2026, 7, 1)
        val transactions = listOf(
            transaction(id = 1, date = july),
            transaction(id = 2, date = july.withDayOfMonth(31)),
            transaction(id = 3, date = july.minusDays(1)),
            transaction(id = 4, date = july.plusMonths(1)),
            transaction(id = 5, date = LocalDate.of(2026, 8, 10)),
            transaction(id = 6, date = LocalDate.of(2026, 8, 12))
        )

        assertEquals(
            listOf(1L, 2L),
            filter(
                transactions,
                dateFilter = TransactionDateFilter.MONTH,
                selectedMonth = july
            ).map { it.id }
        )
        assertEquals(
            listOf(5L, 6L),
            filter(
                transactions,
                dateFilter = TransactionDateFilter.CUSTOM,
                customStart = LocalDate.of(2026, 8, 10),
                customEnd = LocalDate.of(2026, 8, 12)
            ).map { it.id }
        )
        assertEquals(
            emptyList<TransactionEntity>(),
            filter(
                transactions,
                dateFilter = TransactionDateFilter.CUSTOM,
                customStart = null,
                customEnd = null
            )
        )
    }

    @Test
    fun sortingUsesTheExpectedTieBreakers() {
        val transactions = listOf(
            transaction(id = 1, date = LocalDate.of(2026, 8, 20), amountMinor = 500),
            transaction(id = 2, date = LocalDate.of(2026, 8, 20), amountMinor = 100),
            transaction(id = 3, date = LocalDate.of(2026, 8, 21), amountMinor = 100),
            transaction(id = 4, date = LocalDate.of(2026, 8, 19), amountMinor = 500)
        )

        assertEquals(
            listOf(3L, 2L, 1L, 4L),
            sortTransactions(transactions, TransactionSort.NEWEST).map { it.id }
        )
        assertEquals(
            listOf(4L, 1L, 2L, 3L),
            sortTransactions(transactions, TransactionSort.OLDEST).map { it.id }
        )
        assertEquals(
            listOf(1L, 4L, 3L, 2L),
            sortTransactions(transactions, TransactionSort.HIGHEST_AMOUNT).map { it.id }
        )
        assertEquals(
            listOf(3L, 2L, 1L, 4L),
            sortTransactions(transactions, TransactionSort.LOWEST_AMOUNT).map { it.id }
        )
    }

    private fun filter(
        transactions: List<TransactionEntity>,
        searchQuery: String = "",
        typeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
        categoryId: Long = ALL_CATEGORIES_FILTER,
        dateFilter: TransactionDateFilter = TransactionDateFilter.ALL_TIME,
        selectedMonth: LocalDate = today.withDayOfMonth(1),
        customStart: LocalDate? = null,
        customEnd: LocalDate? = null
    ): List<TransactionEntity> = filterTransactions(
        transactions = transactions,
        criteria = TransactionFilterCriteria(
            searchQuery = searchQuery,
            type = typeFilter,
            categoryId = categoryId,
            date = dateFilter,
            selectedMonthEpochDay = selectedMonth.toEpochDay(),
            customStartEpochDay = customStart?.toEpochDay(),
            customEndEpochDay = customEnd?.toEpochDay(),
            today = today
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
