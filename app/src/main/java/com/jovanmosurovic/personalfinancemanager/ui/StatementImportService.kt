package com.jovanmosurovic.personalfinancemanager.ui

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.jovanmosurovic.personalfinancemanager.data.FinanceRepository
import com.jovanmosurovic.personalfinancemanager.data.importer.OtpImportFormat
import com.jovanmosurovic.personalfinancemanager.data.importer.OtpParsedTransaction
import com.jovanmosurovic.personalfinancemanager.data.importer.OtpStatementImportException
import com.jovanmosurovic.personalfinancemanager.data.importer.OtpStatementImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class ImportError {
    UNSUPPORTED_FILE,
    INVALID_FILE,
    NO_TRANSACTIONS,
    READ_FAILED
}

data class ImportSummary(
    val format: OtpImportFormat,
    val importedCount: Int,
    val duplicateCount: Int
) {
    val parsedCount: Int
        get() = importedCount + duplicateCount
}

data class ImportUiState(
    val isImporting: Boolean = false,
    val summary: ImportSummary? = null,
    val error: ImportError? = null
)

internal class StatementImportService(
    context: Context,
    private val repository: FinanceRepository
) {
    private val applicationContext = context.applicationContext
    private val contentResolver = applicationContext.contentResolver
    private val importer = OtpStatementImporter()

    fun findFormat(uri: Uri): OtpImportFormat? {
        return OtpImportFormat.from(
            mimeType = contentResolver.getType(uri),
            fileName = contentResolver.queryDisplayName(uri) ?: uri.lastPathSegment
        )
    }

    suspend fun import(uri: Uri, format: OtpImportFormat): ImportUiState {
        return try {
            val parsedTransactions = readTransactions(uri, format)
            if (parsedTransactions.isEmpty()) {
                ImportUiState(error = ImportError.NO_TRANSACTIONS)
            } else {
                saveTransactions(format, parsedTransactions)
            }
        } catch (_: OtpStatementImportException) {
            ImportUiState(error = ImportError.INVALID_FILE)
        } catch (_: Exception) {
            ImportUiState(error = ImportError.READ_FAILED)
        }
    }

    private suspend fun readTransactions(
        uri: Uri,
        format: OtpImportFormat
    ): List<OtpParsedTransaction> = withContext(Dispatchers.IO) {
        contentResolver.openInputStream(uri)?.use { input ->
            importer.parse(
                format = format,
                input = input,
                context = applicationContext
            )
        } ?: throw OtpStatementImportException()
    }

    private suspend fun saveTransactions(
        format: OtpImportFormat,
        transactions: List<OtpParsedTransaction>
    ): ImportUiState {
        val result = withContext(Dispatchers.IO) {
            repository.importTransactions(transactions)
        }
        return ImportUiState(
            summary = ImportSummary(
                format = format,
                importedCount = result.importedCount,
                duplicateCount = result.duplicateCount
            )
        )
    }
}

private fun ContentResolver.queryDisplayName(uri: Uri): String? = query(
    uri,
    arrayOf(OpenableColumns.DISPLAY_NAME),
    null,
    null,
    null
)?.use { cursor ->
    if (cursor.moveToFirst()) {
        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
    } else {
        null
    }
}
