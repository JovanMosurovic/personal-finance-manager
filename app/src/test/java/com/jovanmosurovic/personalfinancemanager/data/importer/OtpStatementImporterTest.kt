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
    fun parsesOtpPdfTextWithTwoDateFormat() {
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
    fun parsesOtpVirtualCardTransferDescription() {
        val text = """
            05.07.2026 05.07.2026 0,00 5.000,00 Prenos u korist 9120726623676 194.323,71
            28.07.2026 28.07.2026 0,00 6.000,00 m-Banking prenos u korist 9300706420629 (ib-mobile) 420.182,81
            Datum i vreme štampe: 07.08.2026. 16:16:09
            www.otpbanka.rs
            2 od 3
            Vaša OTP banka Srbija Info center: 021 421 077
        """.trimIndent()

        val transactions = pdfTextParser.parse(text)

        assertEquals(2, transactions.size)
        assertEquals("Prenos u korist 9120726623676", transactions[0].merchant)
        assertEquals("m-Banking prenos u korist 9300706420629 (ib-mobile)", transactions[1].merchant)
    }

    @Test
    fun parsesAccountOverviewWithSingleAmountColumn() {
        val text = """
            Datum valute Datum obrade Isplate Uplate Opis
            20.07.2026 20.07.2026 5,000.00 Interni prenos sa računa
            325930070633456009 na račun 325912072662367691
            08.07.2026 08.07.2026 254,589.65 JETBRAINS D.O.O. ZARADA ZA 06.2026
            02.07.2026 02.07.2026 3,000.00 VIOLETA DAMNJANOVIĆ,
            Transakcije po nalogu građana, 9300500124019
            OTP banka Srbija 1/1
        """.trimIndent()

        val transactions = pdfTextParser.parse(text)

        assertEquals(3, transactions.size)
        assertEquals(TransactionType.EXPENSE, transactions[0].type)
        assertEquals(500000L, transactions[0].amountMinor)
        assertEquals(
            "Interni prenos sa računa 325930070633456009 na račun 325912072662367691",
            transactions[0].merchant
        )
        assertEquals(TransactionType.INCOME, transactions[1].type)
        assertEquals(25458965L, transactions[1].amountMinor)
        assertEquals(TransactionType.INCOME, transactions[2].type)
        assertEquals(300000L, transactions[2].amountMinor)
    }
}
