package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jovanmosurovic.personalfinancemanager.R
import com.jovanmosurovic.personalfinancemanager.data.local.entity.CategoryEntity
import com.jovanmosurovic.personalfinancemanager.data.local.entity.TransactionEntity
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionNameFormatter
import com.jovanmosurovic.personalfinancemanager.domain.model.TransactionType
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
internal fun MetricCard(
    label: String,
    value: String,
    accentColor: Color,
    valueBlurred: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickableCard(onClick),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.10f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
                Icon(
                    imageVector = if (accentColor == MaterialTheme.colorScheme.secondary) {
                        Icons.Outlined.ArrowUpward
                    } else {
                        Icons.Outlined.ArrowDownward
                    },
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                modifier = Modifier.amountBlur(valueBlurred),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun SpendingChart(
    transactions: List<TransactionEntity>,
    period: AnalyticsPeriod,
    areAmountsHidden: Boolean,
    emptyLabel: String,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val points = spendingPointsForPeriod(transactions, period)
    val maxAmount = points.maxOfOrNull { it.amountMinor } ?: 0L

    if (maxAmount == 0L || areAmountsHidden) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(132.dp),
            contentAlignment = Alignment.Center
        ) {
            if (areAmountsHidden) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.amounts_hidden_chart),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(emptyLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.tertiary
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val gap = 7.dp.toPx()
                        val barWidth = (size.width - gap * (points.size - 1)) / points.size
                        val index = (offset.x / (barWidth + gap))
                            .roundToInt()
                            .coerceIn(0, points.lastIndex)
                        onDateSelected(points[index].date)
                    }
                }
        ) {
            val bottom = size.height - 8.dp.toPx()
            val top = 8.dp.toPx()
            val chartHeight = bottom - top
            val gap = 7.dp.toPx()
            val barWidth = (size.width - gap * (points.size - 1)) / points.size

            listOf(0f, 0.5f, 1f).forEach { progress ->
                val y = bottom - chartHeight * progress
                drawLine(outlineColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
            }
            points.forEachIndexed { index, point ->
                val barHeight = ((point.amountMinor.toFloat() / maxAmount) * chartHeight)
                    .coerceAtLeast(4.dp.toPx())
                drawRoundRect(
                    color = if (index == points.lastIndex) primaryColor else expenseColor,
                    topLeft = Offset(index * (barWidth + gap), bottom - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(chartDayLabel(points.first().date), style = MaterialTheme.typography.labelSmall)
            Text(chartDayLabel(points.last().date), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun chartDayLabel(date: LocalDate): String = "${date.dayOfMonth}.${date.monthValue}."

@Composable
internal fun CompactTransactionRow(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    areAmountsHidden: Boolean
) {
    val isIncome = transaction.type == TransactionType.INCOME.name
    val accentColor = if (isIncome) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TransactionBadge(transaction, accentColor)
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = TransactionNameFormatter.displayName(transaction.merchant),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (category != null) {
                    categoryLabel(category)
                } else {
                    stringResource(R.string.no_category_assigned)
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Text(
            text = formatMoney(if (isIncome) transaction.amountMinor else -transaction.amountMinor),
            modifier = Modifier.amountBlur(areAmountsHidden),
            style = MaterialTheme.typography.titleMedium,
            color = accentColor,
            maxLines = 1
        )
    }
}

@Composable
internal fun TransactionBadge(transaction: TransactionEntity, color: Color) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.14f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (transaction.type == TransactionType.INCOME.name) {
                    Icons.Outlined.ArrowUpward
                } else {
                    Icons.Outlined.ArrowDownward
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun Modifier.clickableCard(onClick: () -> Unit): Modifier =
    clickable(onClick = onClick)
