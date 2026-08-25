package com.jovanmosurovic.personalfinancemanager.data.importer

import com.jovanmosurovic.personalfinancemanager.domain.model.KnownOtpAccounts
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate
import java.util.Locale
import kotlin.math.absoluteValue

internal class OtpPdfTextParser {
    private val valueParser = OtpValueParser()

    fun parse(text: String): List<OtpParsedTransaction> {
        val lines = text.lines().map { it.replace('\u00A0', ' ').trim() }
        val transactions = mutableListOf<OtpParsedTransaction>()
        var index = 0

        while (index < lines.size) {
            val line = lines[index]
            if (!isTransactionStart(line)) {
                index++
                continue
            }

            val block = mutableListOf(line)
            index++
            while (index < lines.size) {
                val nextLine = lines[index]
                if (isTransactionStart(nextLine) || isPdfFooter(nextLine)) break
                if (nextLine.isNotBlank()) block += nextLine
                index++
            }

            parsePdfBlock(block)?.let(transactions::add)
        }

        return transactions
    }

    private fun parsePdfBlock(block: List<String>): OtpParsedTransaction? {
        val firstLineTokens = block.first().split(otpWhitespaceRegex)
        val firstDate = valueParser.parseDate(firstLineTokens.firstOrNull().orEmpty()) ?: return null
        val secondDate = firstLineTokens.getOrNull(1)?.let(valueParser::parseDate)
        val bodyTokens = firstLineTokens.drop(if (secondDate != null) 2 else 1) +
            block.drop(1).flatMap { it.split(otpWhitespaceRegex) }

        return if (secondDate != null) {
            parseTwoDatePdfBlock(secondDate, bodyTokens)
        } else {
            parseOneDatePdfBlock(firstDate, bodyTokens)
        }
    }

    private fun parseTwoDatePdfBlock(
        date: LocalDate,
        bodyTokens: List<String>
    ): OtpParsedTransaction? {
        val firstAmount = bodyTokens.getOrNull(0)?.let(valueParser::parseAmountMinor) ?: return null
        val secondAmount = bodyTokens.getOrNull(1)?.let(valueParser::parseAmountMinor)
        if (secondAmount == null) {
            val description = bodyTokens.drop(1).joinToString(" ").cleanText()
            val type = if (looksLikeIncome(description)) {
                TransactionType.INCOME
            } else {
                TransactionType.EXPENSE
            }
            return createParsedTransaction(
                type = type,
                amountMinor = firstAmount.absoluteValue,
                description = description,
                date = date
            )
        }

        val incoming = firstAmount
        val outgoing = secondAmount
        val descriptionTokens = bodyTokens.drop(2).toMutableList()
        val balanceIndex = descriptionTokens.indexOfLast(valueParser::isMoneyToken)
        if (balanceIndex >= 0) descriptionTokens.removeAt(balanceIndex)

        val description = descriptionTokens.joinToString(" ").cleanText()
        val typeAndAmount = when {
            incoming != 0L -> TransactionType.INCOME to incoming.absoluteValue
            outgoing != 0L -> TransactionType.EXPENSE to outgoing.absoluteValue
            else -> return null
        }

        return createParsedTransaction(
            type = typeAndAmount.first,
            amountMinor = typeAndAmount.second,
            description = description,
            date = date
        )
    }

    private fun parseOneDatePdfBlock(
        date: LocalDate,
        bodyTokens: List<String>
    ): OtpParsedTransaction? {
        val amountIndices = bodyTokens.mapIndexedNotNull { index, token ->
            if (valueParser.isMoneyToken(token)) index else null
        }
        if (amountIndices.isEmpty()) return null

        val selectedAmount = amountIndices
            .map { bodyTokens[it] }
            .mapNotNull(valueParser::parseAmountMinor)
            .firstOrNull { it != 0L }
            ?: return null
        val description = bodyTokens
            .filterIndexed { index, _ -> index !in amountIndices }
            .joinToString(" ")
            .cleanText()
        val type = if (looksLikeIncome(description)) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }

        return createParsedTransaction(
            type = type,
            amountMinor = selectedAmount.absoluteValue,
            description = description,
            date = date
        )
    }

    private fun createParsedTransaction(
        type: TransactionType,
        amountMinor: Long,
        description: String,
        date: LocalDate
    ): OtpParsedTransaction? {
        val merchant = description.removeOtpPrefix()
        if (merchant.isBlank() || amountMinor <= 0L) return null

        return OtpParsedTransaction(
            type = type,
            amountMinor = amountMinor,
            merchant = merchant,
            note = "",
            dateEpochDay = date.toEpochDay()
        )
    }

    private fun isTransactionStart(line: String): Boolean {
        val firstToken = line.split(otpWhitespaceRegex).firstOrNull().orEmpty()
        return valueParser.parseDate(firstToken) != null
    }

    private fun isPdfFooter(line: String): Boolean {
        val normalized = line.lowercase(Locale.ROOT)
        return normalized.contains("otp banka") ||
            normalized.contains("e-mail:") ||
            normalized.contains("website:") ||
            normalized.contains("datum i vreme štampe") ||
            normalized.contains("www.otpbanka.rs") ||
            normalized.matches(Regex(".*\\b[123]\\s+od\\s+[123]\\b.*")) ||
            normalized.matches(Regex(".*\\b[123]/[123]\\b.*"))
    }

    private fun looksLikeIncome(description: String): Boolean {
        val normalized = description.uppercase(Locale.ROOT)
        return listOf(
            "YETTEL D.O.O.",
            "UPLATA GOTOVINE",
            "M-BANKING PRILIV",
            "PRILIV",
            "PLATA",
            "JETBRAINS",
            KnownOtpAccounts.VIOLETA_DAMNJANOVIC_ACCOUNT
        ).any(normalized::contains)
    }
}
