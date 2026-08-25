package com.jovanmosurovic.personalfinancemanager.data.importer

import android.content.Context
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream
import java.util.Locale

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

class OtpStatementImportException(cause: Throwable? = null) : Exception(cause)

class OtpStatementImporter {
    private val csvParser = OtpCsvParser()
    private val pdfTextParser = OtpPdfTextParser()

    fun parse(
        format: OtpImportFormat,
        input: InputStream,
        context: Context? = null
    ): List<OtpParsedTransaction> = when (format) {
        OtpImportFormat.CSV -> csvParser.parse(input)
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
                pdfTextParser.parse(text)
            }
        } catch (exception: Exception) {
            throw OtpStatementImportException(exception)
        }
    }
}
