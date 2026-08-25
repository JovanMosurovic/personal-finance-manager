package com.jovanmosurovic.personalfinancemanager.ui

import com.jovanmosurovic.personalfinancemanager.data.importer.OtpImportFormat
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class FinanceUiStateTest {
    @Test
    fun calculatesCurrentMonthTotalsByTransactionType() {
        val today = LocalDate.now()
        val previousMonth = today.minusMonths(1)
        val state = FinanceUiState(
            transactions = listOf(
                transaction(TransactionType.INCOME, 100_000L, today),
                transaction(TransactionType.EXPENSE, 25_000L, today),
                transaction(TransactionType.INCOME, 50_000L, previousMonth)
            )
        )

        assertEquals(100_000L, state.incomeThisMonthMinor)
        assertEquals(25_000L, state.expensesThisMonthMinor)
    }

    @Test
    fun derivesParsedCountFromImportResults() {
        val summary = ImportSummary(
            format = OtpImportFormat.CSV,
            importedCount = 4,
            duplicateCount = 2
        )

        assertEquals(6, summary.parsedCount)
    }

    private fun transaction(
        type: TransactionType,
        amountMinor: Long,
        date: LocalDate
    ): TransactionEntity {
        return TransactionEntity(
            type = type.name,
            amountMinor = amountMinor,
            merchant = "Test",
            dateEpochDay = date.toEpochDay()
        )
    }
}
