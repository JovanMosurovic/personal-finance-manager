package com.jovanmosurovic.personalfinancemanager.data.importer

import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

internal class OtpValueParser {
    fun parseTransactionType(raw: String?): TransactionType? {
        val value = normalizeHeader(raw.orEmpty())

        return when (value) {
            in incomeTypeNames -> TransactionType.INCOME
            in expenseTypeNames -> TransactionType.EXPENSE
            else -> null
        }
    }

    fun parseDate(raw: String): LocalDate? {
        val match = dateRegex.find(raw) ?: return null
        val value = match.value.replace('/', '.').replace('-', '.')
        return dateFormatters.firstNotNullOfOrNull { formatter ->
            try {
                LocalDate.parse(value, formatter)
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }

    fun parseAmountMinor(raw: String?): Long? {
        val preparedAmount = prepareAmount(raw) ?: return null
        val normalized = normalizeSeparators(preparedAmount.value)

        return runCatching {
            val amount = BigDecimal(normalized)
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact()

            if (preparedAmount.negateResult) -amount else amount
        }.getOrNull()
    }

    private fun prepareAmount(raw: String?): PreparedAmount? {
        var value = raw
            ?.trim()
            ?.replace("\u00A0", "")
            ?.replace(" ", "")
            ?.replace(rsdSuffixRegex, "")
            ?.trim()
            ?: return null

        if (value.isBlank() || value == "-") return null

        val parenthesized = value.startsWith("(") && value.endsWith(")")
        if (parenthesized) {
            value = value.substring(1, value.length - 1)
        }

        val trailingMinus = value.endsWith('-')
        if (trailingMinus) {
            value = value.dropLast(1)
        }

        val numericValue = value.replace(nonNumericAmountRegex, "")
        if (numericValue.isBlank()) return null

        return PreparedAmount(
            value = numericValue,
            negateResult = parenthesized || trailingMinus
        )
    }

    private fun normalizeSeparators(value: String): String {
        val commaIndex = value.lastIndexOf(',')
        val dotIndex = value.lastIndexOf('.')

        return when {
            commaIndex >= 0 && dotIndex >= 0 && commaIndex > dotIndex -> {
                value.replace(".", "").replace(',', '.')
            }
            commaIndex >= 0 && dotIndex >= 0 -> value.replace(",", "")
            commaIndex >= 0 -> {
                val decimals = value.length - commaIndex - 1
                if (decimals in 1..2) value.replace(',', '.') else value.replace(",", "")
            }
            dotIndex >= 0 -> {
                val decimals = value.length - dotIndex - 1
                if (decimals in 1..2) value else value.replace(".", "")
            }
            else -> value
        }
    }

    fun isMoneyToken(value: String): Boolean {
        val token = value.trim()
        if (token.isBlank() || parseAmountMinor(token) == null) return false
        if (token.any { it == ',' || it == '.' }) {
            val separatorIndex = maxOf(token.lastIndexOf(','), token.lastIndexOf('.'))
            return token.length - separatorIndex - 1 in 1..2
        }
        return token.length <= 4
    }

    fun normalizeHeader(value: String): String = Normalizer
        .normalize(value.removePrefix("\uFEFF").lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace("đ", "d")
        .replace(Regex("\\p{M}+"), "")
        .replace(Regex("[^a-z0-9]"), "")

    private data class PreparedAmount(
        val value: String,
        val negateResult: Boolean
    )

    private companion object {
        val dateRegex = Regex("\\b(?:\\d{1,2}[./]\\d{1,2}[./]\\d{4}|\\d{4}-\\d{1,2}-\\d{1,2})\\b")
        val dateFormatters = listOf(
            DateTimeFormatter.ofPattern("d.M.uuuu"),
            DateTimeFormatter.ofPattern("uuuu.M.d")
        )
        val incomeTypeNames = setOf("income", "uplata", "uplate", "deposit", "credit", "prihod")
        val expenseTypeNames = setOf(
            "expense", "isplata", "isplate", "withdrawal", "debit", "trosak"
        )
        val rsdSuffixRegex = Regex("(?i)RSD$")
        val nonNumericAmountRegex = Regex("[^0-9,.+-]")
    }
}
