package com.jovanmosurovic.personalfinancemanager.domain.model

import java.text.Normalizer
import java.util.Locale

internal object OtpAccountNameResolver {
    private val accountNames = mapOf(
        KnownOtpAccounts.CURRENT_ACCOUNT to "Tekući račun",
        KnownOtpAccounts.FILIP_PETROVIC_ACCOUNT to "Filip Petrović",
        KnownOtpAccounts.VIOLETA_DAMNJANOVIC_ACCOUNT to "Violeta Damnjanović"
    )

    private val internalTransferRegex = Regex(
        "^interni prenos sa racuna (\\d{13,18}) na racun (\\d{13,18})$"
    )
    private val legacyOutgoingTransferRegex = Regex("^prenos u korist (\\d{13,18})$")
    private val mobileOutgoingTransferRegex = Regex(
        "^m-banking prenos u korist (\\d{13,18})(?: \\(ib-mobile\\))?$"
    )
    private val mobileIncomingTransferRegex = Regex(
        "^m-banking priliv sa racuna (\\d{13,18})(?: \\(ib-mobile\\)|\\(ib-mobile\\))?.*$"
    )

    fun resolveName(description: String): String? {
        val normalizedDescription = normalize(description)

        internalTransferRegex.matchEntire(normalizedDescription)?.let { match ->
            return internalTransferName(
                sourceAccount = match.groupValues[1],
                targetAccount = match.groupValues[2]
            )
        }

        legacyOutgoingTransferRegex.matchEntire(normalizedDescription)?.let { match ->
            return legacyOutgoingTransferName(match.groupValues[1])
        }

        mobileOutgoingTransferRegex.matchEntire(normalizedDescription)?.let { match ->
            return outgoingTransferName(match.groupValues[1])
        }

        mobileIncomingTransferRegex.matchEntire(normalizedDescription)?.let { match ->
            val accountName = accountNames[match.groupValues[1]]
            if (accountName != null) return accountName
        }

        return accountNames.entries.firstOrNull { (account, _) ->
            Regex("\\b$account\\b").containsMatchIn(description)
        }?.value
    }

    private fun internalTransferName(sourceAccount: String, targetAccount: String): String {
        val isVirtualCardTransfer = sourceAccount == KnownOtpAccounts.CURRENT_ACCOUNT &&
            targetAccount == KnownOtpAccounts.VIRTUAL_CARD_ACCOUNT

        return if (isVirtualCardTransfer) {
            VIRTUAL_CARD_NAME
        } else {
            outgoingTransferName(targetAccount)
        }
    }

    private fun legacyOutgoingTransferName(targetAccount: String): String {
        return if (targetAccount == KnownOtpAccounts.LEGACY_VIRTUAL_CARD_ACCOUNT) {
            VIRTUAL_CARD_NAME
        } else {
            outgoingTransferName(targetAccount)
        }
    }

    private fun outgoingTransferName(targetAccount: String): String {
        return accountNames[targetAccount] ?: OTHER_PERSON_TRANSFER_NAME
    }

    private fun normalize(value: String): String = Normalizer
        .normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace("đ", "d")
        .replace(Regex("\\p{M}+"), "")
        .replace(Regex("\\s+"), " ")
        .trim()

    private const val VIRTUAL_CARD_NAME = "Virtual card"
    private const val OTHER_PERSON_TRANSFER_NAME = "Prenos drugom licu"
}
