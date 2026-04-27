package com.minimalledger.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalledger.app.data.model.AssetAccount
import com.minimalledger.app.data.model.TransactionType
import com.minimalledger.app.ui.components.TransactionTypeToggle
import com.minimalledger.app.ui.theme.ExpenseRed
import com.minimalledger.app.ui.theme.IncomeGreen
import com.minimalledger.app.ui.theme.LedgerAccent
import com.minimalledger.app.ui.theme.LedgerBackground
import com.minimalledger.app.ui.theme.LedgerBorder
import com.minimalledger.app.ui.theme.LedgerPrimary
import com.minimalledger.app.ui.theme.LedgerSurface
import com.minimalledger.app.utils.formatCurrency
import com.minimalledger.app.viewmodel.LedgerUiState

private data class QuickRecordTemplate(
    val note: String,
    val amountInCents: Long,
    val type: TransactionType,
)

private val expenseNoteOptions = listOf(
    "\u9910\u996E",
    "\u4EA4\u901A",
    "\u8D2D\u7269",
    "\u4F4F\u623F",
    "\u533B\u7597",
    "\u5B66\u4E60",
    "\u5A31\u4E50",
    "\u4EBA\u60C5",
    "\u65E5\u7528",
)

private val incomeNoteOptions = listOf(
    "\u5DE5\u8D44",
    "\u517C\u804C",
    "\u62A5\u9500",
    "\u7EA2\u5305",
    "\u7406\u8D22",
    "\u5956\u91D1",
)

private val quickExpenseRecords = listOf(
    QuickRecordTemplate("\u65E9\u9910", 800, TransactionType.EXPENSE),
    QuickRecordTemplate("\u5348\u9910", 2500, TransactionType.EXPENSE),
    QuickRecordTemplate("\u5496\u5561", 1800, TransactionType.EXPENSE),
    QuickRecordTemplate("\u5730\u94C1", 400, TransactionType.EXPENSE),
    QuickRecordTemplate("\u6253\u8F66", 3500, TransactionType.EXPENSE),
    QuickRecordTemplate("\u623F\u79DF", 150000, TransactionType.EXPENSE),
)

private val quickIncomeRecords = listOf(
    QuickRecordTemplate("\u5DE5\u8D44", 800000, TransactionType.INCOME),
    QuickRecordTemplate("\u517C\u804C", 30000, TransactionType.INCOME),
    QuickRecordTemplate("\u62A5\u9500", 12000, TransactionType.INCOME),
    QuickRecordTemplate("\u7EA2\u5305", 5000, TransactionType.INCOME),
)

@Composable
fun EntryScreen(
    uiState: LedgerUiState,
    onAmountChanged: (String) -> Unit,
    onClearAmount: () -> Unit,
    onDeleteLastDigit: () -> Unit,
    onTypeSelected: (TransactionType) -> Unit,
    onNoteChanged: (String) -> Unit,
    onAssetSelected: (Long?) -> Unit,
    onSuggestionClicked: (String) -> Unit,
    onQuickRecordClicked: (Long, TransactionType, String) -> Unit,
    onSaveClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = if (uiState.selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed
    val noteOptions = if (uiState.selectedType == TransactionType.INCOME) {
        incomeNoteOptions
    } else {
        expenseNoteOptions
    }
    val mergedNoteOptions = (noteOptions + uiState.noteSuggestions.map { it.note })
        .filter { it.isNotBlank() }
        .distinct()
        .take(12)
    val quickRecords = if (uiState.selectedType == TransactionType.INCOME) {
        quickIncomeRecords
    } else {
        quickExpenseRecords
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LedgerPrimary,
                        LedgerBackground,
                        LedgerBackground,
                    ),
                ),
            ),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                AmountHeroCard(
                    amountInput = uiState.amountInput,
                    accentColor = accentColor,
                    onAmountChanged = onAmountChanged,
                    onClearAmount = onClearAmount,
                    onDeleteLastDigit = onDeleteLastDigit,
                )
            }

            item {
                TransactionTypeToggle(
                    selectedType = uiState.selectedType,
                    onSelected = onTypeSelected,
                )
            }

            item {
                AssetSourceSection(
                    assets = uiState.assets,
                    selectedAssetAccountId = uiState.selectedAssetAccountId,
                    selectedType = uiState.selectedType,
                    onAssetSelected = onAssetSelected,
                )
            }

            item {
                QuickNoteSection(
                    title = "\u91D1\u94B1\u7C7B\u578B / \u5E38\u7528\u5907\u6CE8",
                    notes = mergedNoteOptions,
                    onSuggestionClicked = onSuggestionClicked,
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.noteInput,
                    onValueChange = onNoteChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("\u5907\u6CE8") },
                    placeholder = {
                        Text("\u4F8B\u5982\uFF1A\u5348\u996D\u3001\u901A\u52E4\u3001\u5DE5\u8D44")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                )
            }

            item {
                QuickRecordSection(
                    records = quickRecords,
                    onQuickRecordClicked = onQuickRecordClicked,
                )
            }

            item {
                Button(
                    onClick = onSaveClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    enabled = uiState.canSave,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                ) {
                    Text(
                        text = "\u5B8C\u6210\u8BB0\u5F55",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun AssetSourceSection(
    assets: List<AssetAccount>,
    selectedAssetAccountId: Long?,
    selectedType: TransactionType,
    onAssetSelected: (Long?) -> Unit,
) {
    val effectiveSelectedAssetId = selectedAssetAccountId ?: assets.firstOrNull()?.id
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = LedgerSurface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = if (selectedType == TransactionType.INCOME) {
                    "\u6536\u5165\u5230\u54EA\u4E2A\u8D44\u4EA7"
                } else {
                    "\u4ECE\u54EA\u4E2A\u8D44\u4EA7\u652F\u51FA"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (assets.isEmpty()) {
                Text(
                    text = "\u8BF7\u5148\u5230\u201C\u8D44\u4EA7\u201D\u9875\u6DFB\u52A0\u73B0\u91D1\u3001\u94F6\u884C\u5361\u6216\u4FE1\u7528\u5361\uFF0C\u6BCF\u7B14\u8D26\u5355\u90FD\u4F1A\u5F52\u5C5E\u5230\u5BF9\u5E94\u8D44\u4EA7\u3002",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(assets, key = { it.id }) { asset ->
                        AssistChip(
                            onClick = { onAssetSelected(asset.id) },
                            label = {
                                Text(
                                    if (asset.id == effectiveSelectedAssetId) {
                                        "\u2713 ${asset.name}"
                                    } else {
                                        asset.name
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountHeroCard(
    amountInput: String,
    accentColor: androidx.compose.ui.graphics.Color,
    onAmountChanged: (String) -> Unit,
    onClearAmount: () -> Unit,
    onDeleteLastDigit: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = LedgerSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            LedgerSurface,
                            accentColor.copy(alpha = 0.18f),
                        ),
                    ),
                )
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                color = accentColor.copy(alpha = 0.12f),
                contentColor = accentColor,
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = "\u4ECA\u5929\u968F\u624B\u8BB0",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            OutlinedTextField(
                value = amountInput,
                onValueChange = onAmountChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 84.dp),
                label = { Text("\u624B\u52A8\u8F93\u5165\u91D1\u989D") },
                placeholder = { Text("0.00") },
                singleLine = true,
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp,
                    color = LedgerPrimary,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(24.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onClearAmount,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("\u6E05\u7A7A")
                }
                OutlinedButton(
                    onClick = onDeleteLastDigit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("\u9000\u683C")
                }
            }
        }
    }
}

@Composable
private fun QuickNoteSection(
    title: String,
    notes: List<String>,
    onSuggestionClicked: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = LedgerSurface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notes, key = { it }) { note ->
                    AssistChip(
                        onClick = { onSuggestionClicked(note) },
                        label = { Text(note) },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickRecordSection(
    records: List<QuickRecordTemplate>,
    onQuickRecordClicked: (Long, TransactionType, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "\u5E38\u7528\u8BB0\u5F55",
            style = MaterialTheme.typography.titleMedium,
            color = LedgerPrimary,
            fontWeight = FontWeight.Bold,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(records, key = { "${it.type}-${it.note}-${it.amountInCents}" }) { record ->
                CommonRecordCard(
                    record = record,
                    onClick = {
                        onQuickRecordClicked(
                            record.amountInCents,
                            record.type,
                            record.note,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun CommonRecordCard(
    record: QuickRecordTemplate,
    onClick: () -> Unit,
) {
    val color = if (record.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
    Surface(
        onClick = onClick,
        modifier = Modifier.heightIn(min = 104.dp),
        shape = RoundedCornerShape(24.dp),
        color = LedgerSurface,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(color.copy(alpha = 0.12f), LedgerSurface),
                    ),
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = record.note,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LedgerPrimary,
            )
            Text(
                text = formatCurrency(record.amountInCents),
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "\u70B9\u51FB\u586B\u5165",
                style = MaterialTheme.typography.labelMedium,
                color = LedgerAccent,
            )
        }
    }
}
