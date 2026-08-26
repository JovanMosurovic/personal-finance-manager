package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate

@Composable
internal fun AddTransactionScreen(
    onCancel: () -> Unit,
    onSave: (TransactionType, Long, String, String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by rememberSaveable { mutableStateOf(TransactionType.EXPENSE) }
    var amount by rememberSaveable { mutableStateOf("") }
    var merchant by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var selectedDateEpochDay by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var errorMessageRes by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.cancel)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.add_transaction),
                style = MaterialTheme.typography.headlineLarge
            )
        }

        TransactionTypeSelector(
            selectedType = selectedType,
            onTypeSelected = { selectedType = it }
        )

        TextField(
            value = amount,
            onValueChange = {
                amount = it
                errorMessageRes = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.amount)) },
            placeholder = { Text(stringResource(R.string.amount_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = MaterialTheme.shapes.medium,
            colors = financeTextFieldColors()
        )

        TextField(
            value = merchant,
            onValueChange = {
                merchant = it
                errorMessageRes = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.merchant)) },
            placeholder = { Text(stringResource(R.string.merchant_hint)) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = financeTextFieldColors()
        )

        TransactionDateSelector(
            dateEpochDay = selectedDateEpochDay,
            onDateSelected = { selectedDateEpochDay = it }
        )

        TextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.note)) },
            supportingText = { Text(stringResource(R.string.note_optional)) },
            minLines = 2,
            shape = MaterialTheme.shapes.medium,
            colors = financeTextFieldColors()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.automatic_category_info),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        errorMessageRes?.let { messageRes ->
            Text(
                text = stringResource(messageRes),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = {
                val amountMinor = parseAmountToMinor(amount)
                errorMessageRes = when {
                    amountMinor == null || amountMinor <= 0L -> R.string.invalid_amount
                    merchant.isBlank() -> R.string.merchant_required
                    else -> null
                }
                if (errorMessageRes == null && amountMinor != null) {
                    onSave(selectedType, amountMinor, merchant, note, selectedDateEpochDay)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save))
        }
    }
}
