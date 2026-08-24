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
    fun distinguishesVirtualCardTransferFromOtherTransfers() {
        assertEquals(
            "Virtual card",
            TransactionNameFormatter.displayName("Prenos u korist 9120726623676")
        )
        assertEquals(
            "Virtual card",
            TransactionNameFormatter.displayName(
                "Interni prenos sa računa 325930070633456009 " +
                    "na račun 325912072662367691"
            )
        )
        assertEquals(
            "Filip Petrović",
            TransactionNameFormatter.displayName("m-Banking prenos u korist 9300708447362 (ib-mobile)")
        )
        assertEquals(
            "Prenos drugom licu",
            TransactionNameFormatter.displayName("m-Banking prenos u korist 9300706420629 (ib-mobile)")
        )
    }

    @Test
    fun mapsKnownIncomingAccountAndDoesNotOvermatchVirtualCardNumber() {
        assertEquals(
            "Tekući račun",
            TransactionNameFormatter.displayName("Račun 325930070633456009")
        )
        assertEquals(
            "Violeta Damnjanović",
            TransactionNameFormatter.displayName(
                "m-Banking priliv sa računa 9300500124019(ib-mobile) - JOVAN MOSUROVIĆ"
            )
        )
        assertEquals(
            "Referenca",
            TransactionNameFormatter.displayName("Referenca 9120726623676")
        )
        assertEquals(
            "Prenos drugom licu",
            TransactionNameFormatter.displayName(
                "m-Banking prenos u korist 9120726623676 (ib-mobile)"
            )
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
