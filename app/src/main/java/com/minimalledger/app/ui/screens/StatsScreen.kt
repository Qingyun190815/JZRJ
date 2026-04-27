package com.minimalledger.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minimalledger.app.data.model.DailyTrendPoint
import com.minimalledger.app.data.model.MonthlySummary
import com.minimalledger.app.data.model.Transaction
import com.minimalledger.app.ui.components.SummaryCard
import com.minimalledger.app.ui.theme.ExpenseRed
import com.minimalledger.app.ui.theme.IncomeGreen
import com.minimalledger.app.ui.theme.LedgerBorder
import com.minimalledger.app.utils.formatCurrency
import com.minimalledger.app.utils.formatMonthDay
import com.minimalledger.app.utils.formatTime
import com.minimalledger.app.utils.formatYearMonth
import com.minimalledger.app.viewmodel.BudgetStatus
import java.time.YearMonth

@Composable
fun StatsScreen(
    selectedMonth: YearMonth,
    isCurrentMonth: Boolean,
    summary: MonthlySummary,
    budgetStatus: BudgetStatus,
    largestExpense: Transaction?,
    weeklyTrend: List<DailyTrendPoint>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MonthSelector(
            selectedMonth = selectedMonth,
            isCurrentMonth = isCurrentMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onCurrentMonth = onCurrentMonth,
        )

        BudgetRealtimeCard(
            budgetStatus = budgetStatus,
        )

        SummaryCard(
            label = "\u6536\u5165",
            value = formatCurrency(summary.incomeInCents),
            containerColor = IncomeGreen,
        )
        SummaryCard(
            label = "\u652F\u51FA",
            value = formatCurrency(summary.expenseInCents),
            containerColor = ExpenseRed,
        )
        SummaryCard(
            label = "\u7ED3\u4F59",
            value = formatCurrency(summary.balanceInCents),
            containerColor = MaterialTheme.colorScheme.primary,
        )

        MonthlyInsightCard(
            summary = summary,
            largestExpense = largestExpense,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "\u6700\u8FD1 7 \u5929\u8D8B\u52BF",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                WeekTrendChart(points = weeklyTrend)
            }
        }

        Button(
            onClick = onExportCsv,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("\u5BFC\u51FA CSV")
        }
    }
}

@Composable
private fun BudgetRealtimeCard(
    budgetStatus: BudgetStatus,
) {
    val remainingColor = if (budgetStatus.remainingInCents >= 0L) IncomeGreen else ExpenseRed
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "\u5B9E\u65F6\u9884\u7B97\u7ED3\u4F59",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (budgetStatus.isBudgetSet) {
                            "\u9884\u7B97\u6765\u81EA\u8D44\u4EA7\u603B\u989D\uFF0C\u5DF2\u7528 ${budgetStatus.usedPercent}%"
                        } else {
                            "\u9884\u7B97\u6765\u81EA\u8D44\u4EA7\u603B\u989D"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    )
                }
            }

            Text(
                text = if (budgetStatus.isBudgetSet) {
                    formatCurrency(budgetStatus.remainingInCents)
                } else {
                    formatCurrency(budgetStatus.remainingInCents)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = remainingColor,
                fontWeight = FontWeight.Black,
            )

            LinearProgressIndicator(
                progress = { budgetStatus.progress },
                modifier = Modifier.fillMaxWidth(),
                color = remainingColor,
                trackColor = LedgerBorder,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BudgetMetric(
                    label = "\u5468\u671F\u8D44\u4EA7",
                    value = if (budgetStatus.isBudgetSet) formatCurrency(budgetStatus.budgetInCents) else "--",
                    modifier = Modifier.weight(1f),
                )
                BudgetMetric(
                    label = "\u5DF2\u652F\u51FA",
                    value = formatCurrency(budgetStatus.expenseInCents),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BudgetMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MonthSelector(
    selectedMonth: YearMonth,
    isCurrentMonth: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = formatYearMonth(selectedMonth),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("\u4E0A\u4E2A\u6708")
                }
                OutlinedButton(
                    onClick = onCurrentMonth,
                    modifier = Modifier.weight(1f),
                    enabled = !isCurrentMonth,
                ) {
                    Text("\u56DE\u5230\u672C\u6708")
                }
                OutlinedButton(
                    onClick = onNextMonth,
                    modifier = Modifier.weight(1f),
                    enabled = !isCurrentMonth,
                ) {
                    Text("\u4E0B\u4E2A\u6708")
                }
            }
        }
    }
}

@Composable
private fun MonthlyInsightCard(
    summary: MonthlySummary,
    largestExpense: Transaction?,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "\u672C\u6708\u6D1E\u5BDF",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            InsightRow(
                label = "\u652F\u51FA / \u6536\u5165",
                value = expenseIncomeRatio(summary),
            )
            InsightRow(
                label = "\u6700\u5927\u5355\u7B14\u652F\u51FA",
                value = largestExpense?.let {
                    "${formatCurrency(it.amountInCents)}  ${it.note.ifBlank { "\u672A\u586B\u5907\u6CE8" }}"
                } ?: "\u6682\u65E0\u652F\u51FA",
            )
            if (largestExpense != null) {
                Text(
                    text = formatTime(largestExpense.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                )
            }
        }
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun expenseIncomeRatio(summary: MonthlySummary): String {
    return when {
        summary.incomeInCents > 0L -> {
            val ratio = summary.expenseInCents.toDouble() / summary.incomeInCents.toDouble() * 100
            "${ratio.toInt()}%"
        }
        summary.expenseInCents > 0L -> "\u65E0\u6536\u5165"
        else -> "\u6682\u65E0\u6570\u636E"
    }
}

@Composable
private fun WeekTrendChart(points: List<DailyTrendPoint>) {
    val maxAmount = points.maxOfOrNull { maxOf(it.incomeInCents, it.expenseInCents, 1L) } ?: 1L
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        points.forEach { point ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.height(132.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Bar(
                        amount = point.incomeInCents,
                        maxAmount = maxAmount,
                        color = IncomeGreen,
                    )
                    Bar(
                        amount = point.expenseInCents,
                        maxAmount = maxAmount,
                        color = ExpenseRed,
                    )
                }
                Text(
                    text = formatMonthDay(point.date),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun Bar(
    amount: Long,
    maxAmount: Long,
    color: androidx.compose.ui.graphics.Color,
) {
    val ratio = if (maxAmount == 0L) 0f else amount.toFloat() / maxAmount.toFloat()
    Box(
        modifier = Modifier
            .width(18.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(999.dp))
            .background(LedgerBorder),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(ratio.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(999.dp))
                .background(color),
        )
    }
}
