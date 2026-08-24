package com.jovanmosurovic.personalfinancemanager.domain.model

import java.text.Normalizer
import java.util.Locale

object KnownOtpAccounts {
    const val CURRENT_ACCOUNT = "325930070633456009"
    const val VIRTUAL_CARD_ACCOUNT = "325912072662367691"
    const val LEGACY_VIRTUAL_CARD_ACCOUNT = "9120726623676"
    const val FILIP_PETROVIC_ACCOUNT = "9300708447362"
    const val VIOLETA_DAMNJANOVIC_ACCOUNT = "9300500124019"
}

/**
 * Converts a bank's verbose transaction description into a short name for display.
 * The original value is intentionally kept in the database for searching and rules.
 */
object TransactionNameFormatter {
    private data class KnownName(
        val pattern: Regex,
        val displayName: String
    )

    private val knownNames = listOf(
        KnownName(Regex("\\buplata gotovine\\b", RegexOption.IGNORE_CASE), "Uplata gotovine"),
        KnownName(Regex("\\byettel d\\.o\\.o\\.?\\b", RegexOption.IGNORE_CASE), "Yettel"),
        KnownName(Regex("\\bkamata za mesec\\b", RegexOption.IGNORE_CASE), "Kamata za karticu"),
        KnownName(Regex("\\bjkp infostan\\b", RegexOption.IGNORE_CASE), "Infostan"),
        KnownName(Regex("\\btemu\\.com\\b", RegexOption.IGNORE_CASE), "Temu"),
        KnownName(Regex("glovoapp|glovo", RegexOption.IGNORE_CASE), "Glovo"),
        KnownName(Regex("\\bgiros serbia\\b", RegexOption.IGNORE_CASE), "Giros Serbia"),
        KnownName(Regex("\\byettel eracun\\b", RegexOption.IGNORE_CASE), "Yettel račun"),
        KnownName(Regex("\\bmaxi\\b", RegexOption.IGNORE_CASE), "MAXI"),
        KnownName(Regex("\\bc market\\b", RegexOption.IGNORE_CASE), "C MARKET"),
        KnownName(Regex("\\baman\\b", RegexOption.IGNORE_CASE), "AMAN"),
        KnownName(Regex("\\baroma\\b", RegexOption.IGNORE_CASE), "AROMA"),
        KnownName(Regex("\\bhleb i kifle\\b", RegexOption.IGNORE_CASE), "Hleb i kifle"),
        KnownName(Regex("\\btrgocentar\\b", RegexOption.IGNORE_CASE), "Trgocentar"),
        KnownName(Regex("giros i ajskrimos|nutyno", RegexOption.IGNORE_CASE), "Nutyno"),
        KnownName(Regex("\\blidl\\b", RegexOption.IGNORE_CASE), "LIDL"),
        KnownName(Regex("\\bidea\\b", RegexOption.IGNORE_CASE), "IDEA"),
        KnownName(Regex("\\bdm(?: filijala)?\\b", RegexOption.IGNORE_CASE), "dm"),
        KnownName(Regex("\\bgevorest\\b", RegexOption.IGNORE_CASE), "Gevorest"),
        KnownName(Regex("\\bjack (?:&|and) jones\\b", RegexOption.IGNORE_CASE), "Jack & Jones"),
        KnownName(Regex("\\btom tailor\\b", RegexOption.IGNORE_CASE), "Tom Tailor"),
        KnownName(Regex("stampa sistem|moj kiosk", RegexOption.IGNORE_CASE), "Moj kiosk"),
        KnownName(Regex("\\b(?:mol serbia|nis|petrol|coral srb)\\b", RegexOption.IGNORE_CASE), "Gorivo"),
        KnownName(Regex("parking servis|pg vukov spomenik|vepp baba visnjina", RegexOption.IGNORE_CASE), "Parking"),
        KnownName(Regex("gc group", RegexOption.IGNORE_CASE), "Registracija"),
        KnownName(Regex("itunes\\.com", RegexOption.IGNORE_CASE), "Apple subscription"),
        KnownName(Regex("google \\*google one|google one", RegexOption.IGNORE_CASE), "Google One")
    )

    private val knownAccountNames = mapOf(
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

    private val dateRegex = Regex("\\b(?:\\d{1,2}[./-]\\d{1,2}[./-]\\d{4}|\\d{4}-\\d{1,2}-\\d{1,2})\\b")
    private val amountRegex = Regex("\\s+[+-]?(?:\\d{1,3}(?:[.,]\\d{3})*|\\d+)(?:[.,]\\d{2})?(?:\\s*RSD)?(?:\\s|$)", RegexOption.IGNORE_CASE)
    private val footerRegex = Regex(
        "\\s+(?:datum i vreme štampe|www\\.otpbanka\\.rs|vaša otp banka|\\d+\\s+od\\s+\\d+).*",
        RegexOption.IGNORE_CASE
    )
    private val trailingBalanceRegex = Regex("\\s+\\d{1,3}(?:[.]\\d{3})+[,]\\d{2}$")
    private val locationSuffixRegex = Regex(
        "\\s+(?:BEOGRAD|NOVI SAD|NOVI|MLADENOVAC|VOZDOVAC|MIRIJEVO|BANJA|RS)(?:\\s+.*)?$",
        RegexOption.IGNORE_CASE
    )
    private val whitespaceRegex = Regex("\\s+")

    fun displayName(rawName: String): String {
        val cleaned = sourceDescription(rawName)
        if (cleaned.isBlank()) return rawName.trim()

        transferDisplayName(cleaned)?.let { return it }

        knownAccountNames.entries.firstOrNull { (account, _) ->
            Regex("\\b$account\\b").containsMatchIn(cleaned)
        }?.let { return it.value }

        knownNames.firstOrNull { it.pattern.containsMatchIn(cleaned) }?.let {
            return it.displayName
        }

        val shortened = cleaned
            .replace(dateRegex, " ")
            .replace(amountRegex, " ")
            .substringBefore(" | ")
            .substringBefore(" - ")
            .replace(locationSuffixRegex, "")
            .replace(whitespaceRegex, " ")
            .trim(' ', '-', ':', '|')

        return shortened.ifBlank { cleaned }.take(MAX_DISPLAY_LENGTH).trim()
    }

    fun sourceDescription(rawName: String): String {
        val cleaned = rawName
            .replace('\u00A0', ' ')
            .replace(whitespaceRegex, " ")
            .trim()
            .removeOtpPrefix()
        return cleaned
            .replace(footerRegex, "")
            .replace(trailingBalanceRegex, "")
            .replace(whitespaceRegex, " ")
            .trim()
    }

    private fun transferDisplayName(description: String): String? {
        val normalized = description.normalizeForAccountMatching()

        internalTransferRegex.matchEntire(normalized)?.let { match ->
            val sourceAccount = match.groupValues[1]
            val targetAccount = match.groupValues[2]
            if (
                sourceAccount == KnownOtpAccounts.CURRENT_ACCOUNT &&
                targetAccount == KnownOtpAccounts.VIRTUAL_CARD_ACCOUNT
            ) {
                return VIRTUAL_CARD_NAME
            }
            return knownAccountNames[targetAccount] ?: OTHER_PERSON_TRANSFER_NAME
        }

        legacyOutgoingTransferRegex.matchEntire(normalized)?.let { match ->
            val targetAccount = match.groupValues[1]
            if (targetAccount == KnownOtpAccounts.LEGACY_VIRTUAL_CARD_ACCOUNT) {
                return VIRTUAL_CARD_NAME
            }
            return knownAccountNames[targetAccount] ?: OTHER_PERSON_TRANSFER_NAME
        }

        mobileOutgoingTransferRegex.matchEntire(normalized)?.let { match ->
            val targetAccount = match.groupValues[1]
            return knownAccountNames[targetAccount] ?: OTHER_PERSON_TRANSFER_NAME
        }

        mobileIncomingTransferRegex.matchEntire(normalized)?.let { match ->
            return knownAccountNames[match.groupValues[1]]
        }

        return null
    }

    private fun String.normalizeForAccountMatching(): String = Normalizer
        .normalize(lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace("đ", "d")
        .replace(Regex("\\p{M}+"), "")
        .replace(whitespaceRegex, " ")
        .trim()

    private fun String.removeOtpPrefix(): String = replace(
        Regex("^MasterCard Fluo debit\\s*-\\s*", RegexOption.IGNORE_CASE),
        ""
    )

    private const val MAX_DISPLAY_LENGTH = 40
    private const val VIRTUAL_CARD_NAME = "Virtual card"
    private const val OTHER_PERSON_TRANSFER_NAME = "Prenos drugom licu"
}
