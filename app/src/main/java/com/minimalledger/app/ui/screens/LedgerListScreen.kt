package com.minimalledger.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minimalledger.app.data.model.AssetAccount
import com.minimalledger.app.data.model.Transaction
import com.minimalledger.app.ui.components.TransactionRow
import com.minimalledger.app.ui.theme.LedgerAccent
import com.minimalledger.app.ui.theme.LedgerPrimary
import com.minimalledger.app.ui.theme.LedgerSurface
import com.minimalledger.app.utils.formatCurrency
import com.minimalledger.app.utils.formatLedgerDate
import com.minimalledger.app.utils.formatYearMonth
import com.minimalledger.app.viewmodel.LedgerTypeFilter
import com.minimalledger.app.viewmodel.TransactionDayGroup
import java.time.YearMonth

@Composable
fun LedgerListScreen(
    groups: List<TransactionDayGroup>,
    assets: List<AssetAccount>,
    selectedMonth: YearMonth?,
    isCurrentMonth: Boolean,
    searchQuery: String,
    typeFilter: LedgerTypeFilter,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearchQuery: () -> Unit,
    onTypeFilterChanged: (LedgerTypeFilter) -> Unit,
    onAddTransaction: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onShowAllRecords: () -> Unit,
    onShowCurrentMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LedgerMonthFilter(
            selectedMonth = selectedMonth,
            isCurrentMonth = isCurrentMonth,
            searchQuery = searchQuery,
            typeFilter = typeFilter,
            onSearchQueryChanged = onSearchQueryChanged,
            onClearSearchQuery = onClearSearchQuery,
            onTypeFilterChanged = onTypeFilterChanged,
            onAddTransaction = onAddTransaction,
            onShowAllRecords = onShowAllRecords,
            onShowCurrentMonth = onShowCurrentMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
        )

        if (groups.isEmpty()) {
            Text(
                text = "\u8FD8\u6CA1\u6709\u8BB0\u8D26\u8BB0\u5F55",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "\u53EF\u4EE5\u70B9\u51FB\u4E0A\u65B9\u201C\u65B0\u589E\u8D26\u5355\u201D\u76F4\u63A5\u6DFB\u52A0\uFF0C\u6216\u8C03\u6574\u67E5\u8BE2\u6761\u4EF6\u3002",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            groups.forEach { group ->
                item(key = "header-${group.date}") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = formatLedgerDate(group.date),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "\u6536\u5165 ${formatCurrency(group.incomeInCents)} | \u652F\u51FA ${formatCurrency(group.expenseInCents)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        )
                    }
                }

                items(group.transactions, key = { it.id }) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        assetName = assets.firstOrNull { it.id == transaction.assetAccountId }?.name,
                        onDelete = onDeleteTransaction,
                        onClick = onEditTransaction,
                    )
                }
            }
        }
    }
}

@Composable
private fun LedgerMonthFilter(
    selectedMonth: YearMonth?,
    isCurrentMonth: Boolean,
    searchQuery: String,
    typeFilter: LedgerTypeFilter,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearchQuery: () -> Unit,
    onTypeFilterChanged: (LedgerTypeFilter) -> Unit,
    onAddTransaction: () -> Unit,
    onShowAllRecords: () -> Unit,
    onShowCurrentMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = LedgerSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            LedgerSurface,
                            LedgerAccent.copy(alpha = 0.12f),
                        ),
                    ),
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "\u667A\u80FD\u8D26\u5355\u4E2D\u5FC3",
                style = MaterialTheme.typography.titleLarge,
                color = LedgerPrimary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = selectedMonth?.let { "\u5F53\u524D\u67E5\u770B\uFF1A${formatYearMonth(it)}" } ?: "\u5F53\u524D\u67E5\u770B\uFF1A\u5168\u90E8\u8BB0\u5F55",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("\u641C\u7D22\u8D26\u5355") },
                placeholder = { Text("\u8BD5\u8BD5\uFF1A\u5348\u9910\u300120.5\u3001\u6536\u5165\u3001\u652F\u51FA") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = onClearSearchQuery) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "\u6E05\u9664\u641C\u7D22",
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
            )
            TypeFilterRow(
                selectedFilter = typeFilter,
                onFilterSelected = onTypeFilterChanged,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAddTransaction,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LedgerPrimary),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                    )
                    Text("\u65B0\u589E")
                }
                OutlinedButton(
                    onClick = onClearSearchQuery,
                    modifier = Modifier.weight(1f),
                    enabled = searchQuery.isNotBlank(),
                ) {
                    Text("\u6E05\u67E5\u8BE2")
                }
            }
            Text(
                text = "\u70B9\u51FB\u8BB0\u5F55\u4FEE\u6539\uFF0C\u6ED1\u52A8\u5220\u9664\u524D\u4F1A\u4E8C\u6B21\u786E\u8BA4\u3002",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onShowAllRecords,
                    modifier = Modifier.weight(1f),
                    enabled = selectedMonth != null,
                ) {
                    Text("\u5168\u90E8")
                }
                OutlinedButton(
                    onClick = onShowCurrentMonth,
                    modifier = Modifier.weight(1f),
                    enabled = !isCurrentMonth,
                ) {
                    Text("\u672C\u6708")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("\u4E0A\u4E2A\u6708")
                }
                OutlinedButton(
                    onClick = onNextMonth,
                    modifier = Modifier.weight(1f),
                    enabled = selectedMonth == null || !isCurrentMonth,
                ) {
                    Text("\u4E0B\u4E2A\u6708")
                }
            }
        }
    }
}

@Composable
private fun TypeFilterRow(
    selectedFilter: LedgerTypeFilter,
    onFilterSelected: (LedgerTypeFilter) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LedgerTypeFilterButton(
            text = "\u5168\u90E8",
            selected = selectedFilter == LedgerTypeFilter.ALL,
            modifier = Modifier.weight(1f),
        ) { onFilterSelected(LedgerTypeFilter.ALL) }
        LedgerTypeFilterButton(
            text = "\u652F\u51FA",
            selected = selectedFilter == LedgerTypeFilter.EXPENSE,
            modifier = Modifier.weight(1f),
        ) { onFilterSelected(LedgerTypeFilter.EXPENSE) }
        LedgerTypeFilterButton(
            text = "\u6536\u5165",
            selected = selectedFilter == LedgerTypeFilter.INCOME,
            modifier = Modifier.weight(1f),
        ) { onFilterSelected(LedgerTypeFilter.INCOME) }
    }
}

@Composable
private fun LedgerTypeFilterButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LedgerAccent),
        ) {
            Text(text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(text)
        }
    }
}
