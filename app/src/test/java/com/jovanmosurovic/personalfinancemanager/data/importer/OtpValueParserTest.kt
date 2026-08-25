package com.jovanmosurovic.personalfinancemanager.data.importer

import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OtpValueParserTest {
    private val parser = OtpValueParser()

    @Test
    fun parsesSerbianAndEnglishAmountFormats() {
        assertEquals(123_456L, parser.parseAmountMinor("1.234,56"))
        assertEquals(123_456L, parser.parseAmountMinor("1,234.56"))
        assertEquals(500_000L, parser.parseAmountMinor("5,000.00 RSD"))
        assertEquals(500_000L, parser.parseAmountMinor("5.000,00"))
    }

    @Test
    fun parsesAllSupportedNegativeAmountFormats() {
        assertEquals(-125_050L, parser.parseAmountMinor("(1.250,50)"))
        assertEquals(-125_050L, parser.parseAmountMinor("1.250,50-"))
        assertEquals(-125L, parser.parseAmountMinor("-1.25"))
    }

    @Test
    fun rejectsEmptyAmounts() {
        assertNull(parser.parseAmountMinor(null))
        assertNull(parser.parseAmountMinor(""))
        assertNull(parser.parseAmountMinor("-"))
        assertNull(parser.parseAmountMinor("RSD"))
    }

    @Test
    fun parsesDatesAndTransactionTypes() {
        assertEquals(LocalDate.of(2026, 8, 25), parser.parseDate("25.08.2026"))
        assertEquals(LocalDate.of(2026, 8, 25), parser.parseDate("2026-08-25"))
        assertEquals(TransactionType.INCOME, parser.parseTransactionType("Uplata"))
        assertEquals(TransactionType.EXPENSE, parser.parseTransactionType("Isplata"))
        assertNull(parser.parseTransactionType("Nepoznato"))
    }
}
