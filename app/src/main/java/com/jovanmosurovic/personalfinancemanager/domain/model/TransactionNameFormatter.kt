package com.jovanmosurovic.personalfinancemanager.domain.model

/**
 * Converts verbose bank descriptions into short names for display.
 * The original description remains stored for searching and rule matching.
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

    private val dateRegex = Regex("\\b(?:\\d{1,2}[./-]\\d{1,2}[./-]\\d{4}|\\d{4}-\\d{1,2}-\\d{1,2})\\b")
    private val amountRegex = Regex(
        "\\s+[+-]?(?:\\d{1,3}(?:[.,]\\d{3})+(?:[.,]\\d{2})?|\\d+[.,]\\d{2}|\\d+\\s*RSD)(?:\\s|$)",
        RegexOption.IGNORE_CASE
    )
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

    private fun String.removeOtpPrefix(): String = replace(
        Regex("^MasterCard Fluo debit\\s*-\\s*", RegexOption.IGNORE_CASE),
        ""
    )

    private const val MAX_DISPLAY_LENGTH = 40
}
