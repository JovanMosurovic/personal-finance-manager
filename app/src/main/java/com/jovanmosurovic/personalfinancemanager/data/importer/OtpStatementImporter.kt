package com.jovanmosurovic.personalfinancemanager.data.importer

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.math.absoluteValue

enum class OtpImportFormat {
    PDF,
    CSV;

    companion object {
        fun from(mimeType: String?, fileName: String?): OtpImportFormat? {
            val normalizedMimeType = mimeType.orEmpty().lowercase(Locale.ROOT)
            val normalizedFileName = fileName.orEmpty().lowercase(Locale.ROOT)

            return when {
                normalizedMimeType == "application/pdf" || normalizedFileName.endsWith(".pdf") -> PDF
                normalizedMimeType.contains("csv") ||
                    normalizedMimeType == "text/comma-separated-values" ||
                    normalizedMimeType == "application/vnd.ms-excel" ||
                    normalizedFileName.endsWith(".csv") -> CSV
                else -> null
            }
        }
    }
}

data class OtpParsedTransaction(
    val type: TransactionType,
    val amountMinor: Long,
    val merchant: String,
    val note: String,
    val dateEpochDay: Long
)

class OtpStatementImportException(
    val reason: Reason,
    cause: Throwable? = null
) : Exception(cause) {
    enum class Reason {
        INVALID_FILE,
        NO_TRANSACTIONS
    }
}

class OtpStatementImporter {
    fun parse(
        format: OtpImportFormat,
        input: InputStream,
        context: Context? = null
    ): List<OtpParsedTransaction> = when (format) {
        OtpImportFormat.CSV -> parseCsv(input)
        OtpImportFormat.PDF -> {
            requireNotNull(context) { "A context is required to parse a PDF." }
            parsePdf(context, input)
        }
    }

    private fun parsePdf(context: Context, input: InputStream): List<OtpParsedTransaction> {
        return try {
            PDFBoxResourceLoader.init(context.applicationContext)
            PDDocument.load(input).use { document ->
                val text = PDFTextStripper().apply {
                    sortByPosition = true
                }.getText(document)
                parsePdfText(text)
            }
        } catch (exception: OtpStatementImportException) {
            throw exception
        } catch (exception: Exception) {
            throw OtpStatementImportException(
                reason = OtpStatementImportException.Reason.INVALID_FILE,
                cause = exception
            )
        }
    }

    internal fun parseCsv(input: InputStream): List<OtpParsedTransaction> {
        val lines = input.bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readLines().filter { it.isNotBlank() }
        }
        val delimiter = lines.firstOrNull()?.let(::detectDelimiter) ?: ','
        val rows = lines.map { line -> parseCsvLine(line, delimiter) }

        if (rows.isEmpty()) return emptyList()

        val headerRowIndex = rows.indexOfFirst { row ->
            row.any { normalizeHeader(it) in recognizedHeaders }
        }
        val hasHeader = headerRowIndex >= 0
        val header = if (hasHeader) rows[headerRowIndex] else emptyList()
        val dataRows = if (hasHeader) rows.drop(headerRowIndex + 1) else rows

        val dateIndex = findColumn(header, dateHeaders)
        val bookingDateIndex = findColumn(header, bookingDateHeaders)
        val valueDateIndex = findColumn(header, valueDateHeaders)
        val typeIndex = findColumn(header, typeHeaders)
        val amountIndex = findColumn(header, amountHeaders)
        val incomingIndex = findColumn(header, incomingHeaders)
        val outgoingIndex = findColumn(header, outgoingHeaders)
        val merchantIndex = findColumn(header, merchantHeaders)
        val noteIndex = findColumn(header, noteHeaders)

        return dataRows.mapNotNull { row ->
            parseCsvRow(
                dateRaw = row.valueAt(valueDateIndex ?: dateIndex ?: bookingDateIndex ?: 0),
                typeRaw = row.valueAt(typeIndex),
                amountRaw = row.valueAt(amountIndex),
                incomingRaw = row.valueAt(incomingIndex ?: if (!hasHeader) 2 else null),
                outgoingRaw = row.valueAt(outgoingIndex ?: if (!hasHeader) 3 else null),
                merchantRaw = row.valueAt(merchantIndex ?: if (!hasHeader) 4 else null),
                noteRaw = row.valueAt(noteIndex)
            )
        }
    }

    internal fun parsePdfText(text: String): List<OtpParsedTransaction> {
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

    private fun parseCsvRow(
        dateRaw: String?,
        typeRaw: String?,
        amountRaw: String?,
        incomingRaw: String?,
        outgoingRaw: String?,
        merchantRaw: String?,
        noteRaw: String?
    ): OtpParsedTransaction? {
        val date = parseDate(dateRaw.orEmpty()) ?: return null
        val incoming = parseAmountMinor(incomingRaw)
        val outgoing = parseAmountMinor(outgoingRaw)
        val directAmount = parseAmountMinor(amountRaw)
        val explicitType = parseTransactionType(typeRaw)

        val typeAndAmount = when {
            explicitType != null && directAmount != null && directAmount != 0L -> {
                explicitType to directAmount.absoluteValue
            }
            incoming != null && incoming != 0L -> TransactionType.INCOME to incoming.absoluteValue
            outgoing != null && outgoing != 0L -> TransactionType.EXPENSE to outgoing.absoluteValue
            directAmount != null && directAmount != 0L -> {
                val type = explicitType ?: if (directAmount < 0L) {
                    TransactionType.EXPENSE
                } else {
                    TransactionType.EXPENSE
                }
                type to directAmount.absoluteValue
            }
            else -> return null
        }

        val note = noteRaw.orEmpty().cleanText()
        val merchant = merchantRaw.orEmpty().cleanText().ifBlank { note }
        if (merchant.isBlank()) return null

        return OtpParsedTransaction(
            type = typeAndAmount.first,
            amountMinor = typeAndAmount.second,
            merchant = merchant.removeOtpPrefix(),
            note = if (note.equals(merchant, ignoreCase = true)) "" else note,
            dateEpochDay = date.toEpochDay()
        )
    }

    private fun parsePdfBlock(block: List<String>): OtpParsedTransaction? {
        if (block.isEmpty()) return null

        val firstLineTokens = block.first().split(whitespaceRegex)
        val firstDate = parseDate(firstLineTokens.firstOrNull().orEmpty()) ?: return null
        val secondDate = firstLineTokens.getOrNull(1)?.let(::parseDate)
        val bodyTokens = firstLineTokens.drop(if (secondDate != null) 2 else 1) +
            block.drop(1).flatMap { it.split(whitespaceRegex) }

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
        val incoming = bodyTokens.getOrNull(0)?.let(::parseAmountMinor) ?: return null
        val outgoing = bodyTokens.getOrNull(1)?.let(::parseAmountMinor) ?: return null
        val descriptionTokens = bodyTokens.drop(2).toMutableList()
        val balanceIndex = descriptionTokens.indexOfLast(::isMoneyToken)
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
            if (isMoneyToken(token)) index else null
        }
        if (amountIndices.isEmpty()) return null

        val selectedAmount = amountIndices
            .map { bodyTokens[it] }
            .mapNotNull(::parseAmountMinor)
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

    private fun parseTransactionType(raw: String?): TransactionType? {
        val value = normalizeHeader(raw.orEmpty())
        return when {
            value in setOf("income", "uplata", "uplate", "deposit", "credit", "prihod") -> {
                TransactionType.INCOME
            }
            value in setOf("expense", "isplata", "isplate", "withdrawal", "debit", "trosak") -> {
                TransactionType.EXPENSE
            }
            else -> null
        }
    }

    private fun parseDate(raw: String): LocalDate? {
        val match = dateRegex.find(raw) ?: return null
        val value = match.value.replace('/', '.').replace('-', '.')
        val formatters = listOf(
            DateTimeFormatter.ofPattern("d.M.uuuu"),
            DateTimeFormatter.ofPattern("uuuu.M.d")
        )
        return formatters.firstNotNullOfOrNull { formatter ->
            try {
                LocalDate.parse(value, formatter)
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }

    private fun parseAmountMinor(raw: String?): Long? {
        var value = raw?.trim()
            ?.replace("\u00A0", "")
            ?.replace(" ", "")
            ?.replace(Regex("(?i)RSD$"), "")
            ?.trim()
            ?: return null
        if (value.isBlank() || value == "-") return null

        val parenthesized = value.startsWith("(") && value.endsWith(")")
        if (parenthesized) value = value.substring(1, value.length - 1)
        val trailingMinus = value.endsWith('-')
        if (trailingMinus) value = value.dropLast(1)
        value = value.replace(Regex("[^0-9,.+-]"), "")
        if (value.isBlank()) return null

        val commaIndex = value.lastIndexOf(',')
        val dotIndex = value.lastIndexOf('.')
        val normalized = when {
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

        return runCatching {
            val amount = BigDecimal(normalized)
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact()
            if (parenthesized || trailingMinus) -amount else amount
        }.getOrNull()
    }

    private fun isTransactionStart(line: String): Boolean {
        val firstToken = line.split(whitespaceRegex).firstOrNull().orEmpty()
        return parseDate(firstToken) != null
    }

    private fun isPdfFooter(line: String): Boolean {
        val normalized = line.lowercase(Locale.ROOT)
        return normalized.contains("otp banka") ||
            normalized.contains("e-mail:") ||
            normalized.contains("website:") ||
            normalized.matches(Regex(".*\\b[123]/[123]\\b.*"))
    }

    private fun isMoneyToken(value: String): Boolean {
        val token = value.trim()
        if (token.isBlank() || parseAmountMinor(token) == null) return false
        if (token.any { it == ',' || it == '.' }) {
            val separatorIndex = maxOf(token.lastIndexOf(','), token.lastIndexOf('.'))
            return token.length - separatorIndex - 1 in 1..2
        }
        return token.length <= 4
    }

    private fun looksLikeIncome(description: String): Boolean {
        val normalized = description.uppercase(Locale.ROOT)
        return listOf(
            "YETTEL D.O.O.",
            "UPLATA GOTOVINE",
            "M-BANKING PRILIV",
            "PRILIV",
            "PLATA",
            "JETBRAINS"
        ).any(normalized::contains)
    }

    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val values = mutableListOf<String>()
        val current = StringBuilder()
        var quoted = false
        var index = 0

        while (index < line.length) {
            val character = line[index]
            when {
                character == '"' && quoted && index + 1 < line.length && line[index + 1] == '"' -> {
                    current.append('"')
                    index++
                }
                character == '"' -> quoted = !quoted
                character == delimiter && !quoted -> {
                    values += current.toString()
                    current.clear()
                }
                else -> current.append(character)
            }
            index++
        }

        values += current.toString()
        return values
    }

    private fun detectDelimiter(line: String): Char {
        val semicolonCount = line.count { it == ';' }
        val commaCount = line.count { it == ',' }
        return if (semicolonCount > commaCount) ';' else ','
    }

    private fun findColumn(header: List<String>, aliases: Set<String>): Int? =
        header.indexOfFirst { normalizeHeader(it) in aliases }.takeIf { it >= 0 }

    private fun normalizeHeader(value: String): String = Normalizer
        .normalize(value.removePrefix("\uFEFF").lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace("đ", "d")
        .replace(Regex("\\p{M}+"), "")
        .replace(Regex("[^a-z0-9]"), "")

    private fun String.cleanText(): String = replace(whitespaceRegex, " ").trim()

    private fun String.removeOtpPrefix(): String = replace(
        Regex("^MasterCard Fluo debit - ", RegexOption.IGNORE_CASE),
        ""
    ).cleanText()

    private fun List<String>.valueAt(index: Int?): String? = index?.let { getOrNull(it) }

    private companion object {
        val whitespaceRegex = Regex("\\s+")
        val dateRegex = Regex("\\b(?:\\d{1,2}[./]\\d{1,2}[./]\\d{4}|\\d{4}-\\d{1,2}-\\d{1,2})\\b")
        val dateHeaders = setOf("date", "datum", "datumvalute", "datumknjizenja", "datumobrade")
        val bookingDateHeaders = setOf("datumknjizenja", "bookingdate", "posteddate")
        val valueDateHeaders = setOf("datumvalute", "valuedate", "date", "datum")
        val typeHeaders = setOf("type", "transactiontype", "tip", "vrsta")
        val amountHeaders = setOf("amount", "iznos", "value", "transactionamount")
        val incomingHeaders = setOf("uplate", "uplata", "deposit", "deposits", "income", "credit", "prihod")
        val outgoingHeaders = setOf("isplate", "isplata", "withdrawal", "withdrawals", "expense", "debit", "trosak")
        val merchantHeaders = setOf("opis", "description", "merchant", "expense", "payee", "naziv")
        val noteHeaders = setOf("note", "notes", "details", "detail", "napomena", "beleška", "beleska")
        val recognizedHeaders = dateHeaders + typeHeaders + amountHeaders +
            incomingHeaders + outgoingHeaders + merchantHeaders + noteHeaders
    }
}
