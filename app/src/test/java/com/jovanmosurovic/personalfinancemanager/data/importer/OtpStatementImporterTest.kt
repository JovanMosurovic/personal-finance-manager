package com.jovanmosurovic.personalfinancemanager.data.importer

import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpStatementImporterTest {
    private val importer = OtpStatementImporter()
    private val pdfTextParser = OtpPdfTextParser()

    @Test
    fun parsesOtpCsvWithSerbianColumns() {
        val csv = """
            Datum knjiženja,Datum valute,Uplate,Isplate,Opis,Stanje
            21.08.2026,21.08.2026,"0,00","1.234,56",MAXI BEOGRAD,"10.000,00"
            22.08.2026,22.08.2026,"2.000,00","0,00",JETBRAINS D.O.O.,"12.000,00"
        """.trimIndent()

        val transactions = importer.parse(
            format = OtpImportFormat.CSV,
            input = ByteArrayInputStream(csv.toByteArray())
        )

        assertEquals(2, transactions.size)
        assertEquals(TransactionType.EXPENSE, transactions[0].type)
        assertEquals(123456L, transactions[0].amountMinor)
        assertEquals("MAXI BEOGRAD", transactions[0].merchant)
        assertEquals(TransactionType.INCOME, transactions[1].type)
        assertEquals(200000L, transactions[1].amountMinor)
    }

    @Test
    fun parsesNormalizedCsvWithExplicitType() {
        val csv = "date,type,amount,currency,merchant,note\n" +
            "2026-08-23,EXPENSE,1250.50,RSD,Glovo,Delivery"

        val transactions = importer.parse(
            format = OtpImportFormat.CSV,
            input = ByteArrayInputStream(csv.toByteArray())
        )

        assertEquals(1, transactions.size)
        assertEquals(TransactionType.EXPENSE, transactions.single().type)
        assertEquals(125050L, transactions.single().amountMinor)
        assertEquals("Glovo", transactions.single().merchant)
        assertEquals("Delivery", transactions.single().note)
    }

    @Test
    fun parsesOtpPdfTextAndRemovesBalanceFromDescription() {
        val text = """
            OTP BANKA PROMET
            DATUM KNJIŽENJA DATUM VALUTE UPLATE ISPLATE OPIS STANJE
            21.08.2026 21.08.2026 0,00 1.234,56 MAXI BEOGRAD 10.000,00
            22.08.2026 22.08.2026 2.000,00 0,00 JETBRAINS D.O.O. 12.000,00
            OTP banka 1/1
        """.trimIndent()

        val transactions = pdfTextParser.parse(text)

        assertEquals(2, transactions.size)
        assertEquals(TransactionType.EXPENSE, transactions[0].type)
        assertEquals(123456L, transactions[0].amountMinor)
        assertEquals(TransactionType.INCOME, transactions[1].type)
        assertEquals(200000L, transactions[1].amountMinor)
        assertTrue(transactions.none { it.merchant.contains("10.000") })
    }

    @Test
    fun parsesOtpPdfTextWithSingleDateFormat() {
        val text = """
            PROMET PO RAČUNU
            DATUM VALUTE OPIS ISPLATE
            23.08.2026 GLOVOAPP BEOGRAD 950,00
        """.trimIndent()

        val transactions = pdfTextParser.parse(text)

        assertEquals(1, transactions.size)
        assertEquals(TransactionType.EXPENSE, transactions.single().type)
        assertEquals(95000L, transactions.single().amountMinor)
        assertEquals("GLOVOAPP BEOGRAD", transactions.single().merchant)
    }

    @Test
    fun parsesPdfAccountOverviewWithoutAccountSpecificMapping() {
        val text = """
            Datum valute Datum obrade Isplate Uplate Opis
            20.07.2026 20.07.2026 5,000.00 Interni prenos sa računa
            08.07.2026 08.07.2026 254,589.65 JETBRAINS D.O.O. ZARADA ZA 06.2026
            02.07.2026 02.07.2026 3,000.00 Transakcije po nalogu građana
            OTP banka Srbija 1/1
        """.trimIndent()

        val transactions = pdfTextParser.parse(text)

        assertEquals(3, transactions.size)
        assertEquals(TransactionType.EXPENSE, transactions[0].type)
        assertEquals(TransactionType.INCOME, transactions[1].type)
        assertEquals(TransactionType.INCOME, transactions[2].type)
    }
}
