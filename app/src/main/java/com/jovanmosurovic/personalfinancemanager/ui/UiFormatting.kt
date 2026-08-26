package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
internal fun categoryLabel(category: CategoryEntity): String =
    if (category.isSystem) {
        stringResource(categoryLabelRes(category.nameKey))
    } else {
        category.nameKey
    }

private fun categoryLabelRes(nameKey: String): Int = when (nameKey) {
    "category_food" -> R.string.category_food
    "category_groceries" -> R.string.category_groceries
    "category_delivery" -> R.string.category_delivery
    "category_utilities" -> R.string.category_utilities
    "category_phone_internet" -> R.string.category_phone_internet
    "category_electricity" -> R.string.category_electricity
    "category_car" -> R.string.category_car
    "category_fuel" -> R.string.category_fuel
    "category_income" -> R.string.category_income
    "category_salary" -> R.string.category_salary
    "category_shopping" -> R.string.category_shopping
    "category_online" -> R.string.category_online
    else -> R.string.uncategorized
}

internal fun parseAmountToMinor(value: String): Long? = runCatching {
    BigDecimal(value.trim().replace(',', '.'))
        .movePointRight(2)
        .setScale(0, RoundingMode.HALF_UP)
        .longValueExact()
}.getOrNull()

internal fun formatEditableAmount(amountMinor: Long): String =
    BigDecimal.valueOf(amountMinor, 2)
        .stripTrailingZeros()
        .toPlainString()

internal fun Modifier.amountBlur(areAmountsHidden: Boolean): Modifier =
    if (areAmountsHidden) blur(12.dp, BlurredEdgeTreatment.Unbounded) else this

private class DisplayFormatters(val locale: Locale) {
    val money: NumberFormat = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val date: DateTimeFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(locale)
    val monthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", locale)
}

private val displayFormatters = ThreadLocal<DisplayFormatters>()

private fun currentDisplayFormatters(): DisplayFormatters {
    val locale = Locale.getDefault()
    return displayFormatters.get()?.takeIf { it.locale == locale }
        ?: DisplayFormatters(locale).also(displayFormatters::set)
}

internal fun formatMoney(amountMinor: Long): String {
    val formatter = currentDisplayFormatters().money
    return "${formatter.format(BigDecimal.valueOf(amountMinor, 2))} RSD"
}

internal fun formatDate(epochDay: Long): String =
    currentDisplayFormatters().date
        .format(LocalDate.ofEpochDay(epochDay))

internal fun formatMonthYear(date: LocalDate): String =
    currentDisplayFormatters().monthYear.format(date)

internal fun Long.toDatePickerMillis(): Long =
    LocalDate.ofEpochDay(this)
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

internal fun Long.datePickerMillisToEpochDay(): Long =
    Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toEpochDay()
