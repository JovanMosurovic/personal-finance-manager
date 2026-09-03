package com.jovanmosurovic.personalfinancemanager.ui

import android.Manifest
import android.os.Build
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.importer.OtpImportFormat
import java.util.Locale

private enum class AppLanguage(
    val languageTag: String,
    val labelRes: Int
) {
    SERBIAN_LATIN("sr-Latn", R.string.language_serbian_latin),
    ENGLISH("en", R.string.language_english)
}

@Composable
internal fun SettingsScreen(
    importState: ImportUiState,
    areAmountsHidden: Boolean,
    isStatementReminderEnabled: Boolean,
    isBiometricLockEnabled: Boolean,
    biometricSetupError: Boolean,
    onAmountsVisibilityChanged: (Boolean) -> Unit,
    onStatementReminderChanged: (Boolean) -> Unit,
    onBiometricLockChanged: (Boolean) -> Unit,
    onImportFile: (Uri) -> Unit,
    onDismissImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedLanguage = remember {
        currentAppLanguage()
    }
    val context = LocalContext.current
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let(onImportFile) }
    )
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) onStatementReminderChanged(true)
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.language_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.language_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                AppLanguage.entries.forEach { language ->
                    LanguageOption(
                        language = language,
                        selected = language == selectedLanguage,
                        onClick = {
                            if (language != selectedLanguage) {
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags(language.languageTag)
                                )
                            }
                        }
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.privacy_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.privacy_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = areAmountsHidden,
                    onCheckedChange = onAmountsVisibilityChanged,
                    colors = financeSwitchColors()
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.biometric_lock_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = stringResource(R.string.biometric_lock_settings_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isBiometricLockEnabled,
                        onCheckedChange = onBiometricLockChanged,
                        colors = financeSwitchColors()
                    )
                }
                if (biometricSetupError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.biometric_unavailable),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.import_otp_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.import_otp_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.import_otp_formats),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    enabled = !importState.isImporting,
                    onClick = {
                        importLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "text/csv",
                                "text/comma-separated-values",
                                "application/vnd.ms-excel"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (importState.isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (importState.isImporting) {
                            stringResource(R.string.import_otp_importing)
                        } else {
                            stringResource(R.string.import_otp_action)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.statement_reminder_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = stringResource(R.string.statement_reminder_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isStatementReminderEnabled,
                        onCheckedChange = { enabled ->
                            if (!enabled) {
                                onStatementReminderChanged(false)
                            } else if (
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            } else {
                                onStatementReminderChanged(true)
                            }
                        },
                        colors = financeSwitchColors()
                    )
                }
            }
        }
    }

    importState.summary?.let { summary ->
        val formatLabel = when (summary.format) {
            OtpImportFormat.PDF -> stringResource(R.string.import_otp_pdf)
            OtpImportFormat.CSV -> stringResource(R.string.import_otp_csv)
        }
        AlertDialog(
            onDismissRequest = onDismissImport,
            title = { Text(stringResource(R.string.import_otp_success_title)) },
            text = {
                Text(
                    text = if (summary.duplicateCount > 0) {
                        pluralStringResource(
                            R.plurals.import_otp_success_with_duplicates,
                            summary.importedCount,
                            summary.importedCount,
                            summary.parsedCount,
                            formatLabel,
                            summary.duplicateCount
                        )
                    } else {
                        pluralStringResource(
                            R.plurals.import_otp_success,
                            summary.importedCount,
                            summary.importedCount,
                            summary.parsedCount,
                            formatLabel
                        )
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissImport) {
                    Text(stringResource(R.string.import_otp_done))
                }
            }
        )
    }

    importState.error?.let { error ->
        val messageRes = when (error) {
            ImportError.UNSUPPORTED_FILE -> R.string.import_otp_error_unsupported_file
            ImportError.INVALID_FILE -> R.string.import_otp_error_invalid_file
            ImportError.NO_TRANSACTIONS -> R.string.import_otp_error_no_transactions
            ImportError.READ_FAILED -> R.string.import_otp_error_read_failed
        }
        AlertDialog(
            onDismissRequest = onDismissImport,
            title = { Text(stringResource(R.string.import_otp_error_title)) },
            text = { Text(stringResource(messageRes)) },
            confirmButton = {
                TextButton(onClick = onDismissImport) {
                    Text(stringResource(R.string.import_otp_done))
                }
            }
        )
    }
}

@Composable
private fun LanguageOption(
    language: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                } else {
                    Color.Transparent
                },
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics { role = Role.RadioButton },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(language.labelRes))
    }
}

private fun currentAppLanguage(): AppLanguage {
    val applicationTag = AppCompatDelegate
        .getApplicationLocales()
        .toLanguageTags()

    if (applicationTag.startsWith("sr", ignoreCase = true)) {
        return AppLanguage.SERBIAN_LATIN
    }

    if (applicationTag.startsWith("en", ignoreCase = true)) {
        return AppLanguage.ENGLISH
    }

    return if (Locale.getDefault().language.equals("sr", ignoreCase = true)) {
        AppLanguage.SERBIAN_LATIN
    } else {
        AppLanguage.ENGLISH
    }
}
