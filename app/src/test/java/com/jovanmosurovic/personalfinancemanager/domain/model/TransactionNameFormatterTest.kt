package com.jovanmosurovic.personalfinancemanager.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionNameFormatterTest {
    @Test
    fun mapsKnownOtpMerchantToShortName() {
        assertEquals(
            "Glovo",
            TransactionNameFormatter.displayName("MasterCard Fluo debit - GLOVOAPP BEOGRAD")
        )
        assertEquals(
            "MAXI",
            TransactionNameFormatter.displayName("415 - MAXI SM MIRIJEVO")
        )
    }

    @Test
    fun removesDatesAmountsAndLocationFromUnknownDescription() {
        assertEquals(
            "LOCAL SHOP",
            TransactionNameFormatter.displayName("21.08.2026 LOCAL SHOP BEOGRAD 1.250,00")
        )
    }

    @Test
    fun keepsSimpleManualNameUnchanged() {
        assertEquals(
            "Rent",
            TransactionNameFormatter.displayName("Rent")
        )
    }

    @Test
    fun leavesUnmappedTransferDescriptionReadable() {
        assertEquals(
            "Prenos u korist 9120726623676",
            TransactionNameFormatter.displayName("Prenos u korist 9120726623676")
        )
    }

    @Test
    fun removesOtpFooterFromSourceDescription() {
        assertEquals(
            "Prenos u korist 9120726623676",
            TransactionNameFormatter.sourceDescription(
                "Prenos u korist 9120726623676 444.636,05 Datum i vreme štampe: " +
                    "07.08.2026. 16:16:09 www.otpbanka.rs 2 od 3"
            )
        )
    }
}
