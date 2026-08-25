package com.jovanmosurovic.personalfinancemanager.data.importer

import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.io.InputStream
import kotlin.math.absoluteValue

internal class OtpCsvParser {
    private val valueParser = OtpValueParser()

    fun parse(input: InputStream): List<OtpParsedTransaction> {
        val lines = input.bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readLines().filter { it.isNotBlank() }
        }
        val delimiter = lines.firstOrNull()?.let(::detectDelimiter) ?: ','
        val rows = lines.map { line -> parseCsvLine(line, delimiter) }

        if (rows.isEmpty()) return emptyList()

        val headerRowIndex = rows.indexOfFirst { row ->
            row.any { valueParser.normalizeHeader(it) in recognizedHeaders }
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

    private fun parseCsvRow(
        dateRaw: String?,
        typeRaw: String?,
        amountRaw: String?,
        incomingRaw: String?,
        outgoingRaw: String?,
        merchantRaw: String?,
        noteRaw: String?
    ): OtpParsedTransaction? {
        val date = valueParser.parseDate(dateRaw.orEmpty()) ?: return null
        val incoming = valueParser.parseAmountMinor(incomingRaw)
        val outgoing = valueParser.parseAmountMinor(outgoingRaw)
        val directAmount = valueParser.parseAmountMinor(amountRaw)
        val explicitType = valueParser.parseTransactionType(typeRaw)

        val parsedAmount = resolveAmount(
            explicitType = explicitType,
            directAmount = directAmount,
            incoming = incoming,
            outgoing = outgoing
        ) ?: return null

        val note = noteRaw.orEmpty().cleanText()
        val merchant = merchantRaw.orEmpty().cleanText().ifBlank { note }
        if (merchant.isBlank()) return null

        return OtpParsedTransaction(
            type = parsedAmount.type,
            amountMinor = parsedAmount.amountMinor,
            merchant = merchant.removeOtpPrefix(),
            note = if (note.equals(merchant, ignoreCase = true)) "" else note,
            dateEpochDay = date.toEpochDay()
        )
    }

    private fun resolveAmount(
        explicitType: TransactionType?,
        directAmount: Long?,
        incoming: Long?,
        outgoing: Long?
    ): ParsedAmount? {
        if (explicitType != null && directAmount != null && directAmount != 0L) {
            return ParsedAmount(explicitType, directAmount.absoluteValue)
        }
        if (incoming != null && incoming != 0L) {
            return ParsedAmount(TransactionType.INCOME, incoming.absoluteValue)
        }
        if (outgoing != null && outgoing != 0L) {
            return ParsedAmount(TransactionType.EXPENSE, outgoing.absoluteValue)
        }
        if (directAmount != null && directAmount != 0L) {
            return ParsedAmount(TransactionType.EXPENSE, directAmount.absoluteValue)
        }
        return null
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

    private fun findColumn(header: List<String>, aliases: Set<String>): Int? {
        return header.indexOfFirst { valueParser.normalizeHeader(it) in aliases }
            .takeIf { it >= 0 }
    }

    private fun List<String>.valueAt(index: Int?): String? = index?.let { getOrNull(it) }

    private data class ParsedAmount(
        val type: TransactionType,
        val amountMinor: Long
    )

    private companion object {
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
