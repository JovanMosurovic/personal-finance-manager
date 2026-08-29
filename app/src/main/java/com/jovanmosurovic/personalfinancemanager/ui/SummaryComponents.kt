package com.jovanmosurovic.personalfinancemanager.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.semantics.Role
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
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.10f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    modifier = Modifier
                        .weight(1f)
                        .amountBlur(valueBlurred),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
        }
    }
}

@Composable
internal fun SpendingChart(
    transactions: List<TransactionEntity>,
    emptyLabel: String,
    areAmountsHidden: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    period: AnalyticsPeriod = AnalyticsPeriod.LAST_7_DAYS,
    range: AnalyticsDateRange? = null,
    selectedDate: LocalDate? = null
) {
    val points = spendingPointsForPeriod(transactions, period, range)
    val maxAmount = points.maxOfOrNull { it.amountMinor } ?: 0L
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.tertiary

    if (maxAmount == 0L) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(128.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    if (areAmountsHidden) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(128.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.VisibilityOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.amounts_hidden_chart),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .pointerInput(points, onDateSelected) {
                    detectTapGestures { tapOffset ->
                        val gap = if (points.size > 14) 3.dp.toPx() else 7.dp.toPx()
                        val barWidth = (size.width - gap * (points.size - 1)) / points.size
                        val slotWidth = barWidth + gap
                        val index = (tapOffset.x / slotWidth)
                            .roundToInt()
                            .coerceIn(0, points.lastIndex)
                        onDateSelected(points[index].date)
                    }
                }
        ) {
            val bottom = size.height - 8.dp.toPx()
            val top = 8.dp.toPx()
            val chartHeight = bottom - top
            val gap = if (points.size > 14) 3.dp.toPx() else 7.dp.toPx()
            val barWidth = (size.width - gap * (points.size - 1)) / points.size

            listOf(0f, 0.5f, 1f).forEach { progress ->
                val y = bottom - (chartHeight * progress)
                drawLine(
                    color = outlineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            points.forEachIndexed { index, point ->
                val barHeight = ((point.amountMinor.toFloat() / maxAmount) * chartHeight)
                    .coerceAtLeast(4.dp.toPx())
                val x = index * (barWidth + gap)
                val isSelected = selectedDate == point.date
                val barColor = when {
                    isSelected -> primaryColor
                    selectedDate != null -> expenseColor.copy(alpha = 0.68f)
                    index == points.lastIndex -> primaryColor
                    else -> expenseColor
                }
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, bottom - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            val labelStep = when {
                points.size <= 7 -> 1
                points.size <= 14 -> 2
                else -> 5
            }
            points.forEachIndexed { index, point ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (index == 0 || index == points.lastIndex || index % labelStep == 0) {
                        Text(
                            text = chartDayLabel(point.date, points.size > 7),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

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
        TransactionBadge(transaction = transaction, color = accentColor)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = TransactionNameFormatter.displayName(transaction.merchant),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Text(
                text = category?.let { categoryLabel(it) }
                    ?: stringResource(R.string.no_category_assigned),
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
internal fun TransactionBadge(
    transaction: TransactionEntity,
    color: Color
) {
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
