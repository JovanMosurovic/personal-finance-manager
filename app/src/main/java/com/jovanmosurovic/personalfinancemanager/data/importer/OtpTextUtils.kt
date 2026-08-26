package com.jovanmosurovic.personalfinancemanager.data.importer

internal val otpWhitespaceRegex = Regex("\\s+")
private val otpPrefixRegex = Regex(
    "^MasterCard Fluo debit - ",
    RegexOption.IGNORE_CASE
)

internal fun String.cleanText(): String =
    replace(otpWhitespaceRegex, " ").trim()

internal fun String.removeOtpPrefix(): String =
    replace(otpPrefixRegex, "").cleanText()
