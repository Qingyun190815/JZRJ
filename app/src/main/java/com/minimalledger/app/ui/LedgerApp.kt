package com.minimalledger.app.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minimalledger.app.data.model.AssetAccount
import com.minimalledger.app.data.model.AssetType
import com.minimalledger.app.data.model.Transaction
import com.minimalledger.app.data.model.TransactionType
import com.minimalledger.app.data.repository.AssetRepository
import com.minimalledger.app.data.repository.BudgetRepository
import com.minimalledger.app.data.repository.TransactionRepository
import com.minimalledger.app.ui.theme.ExpenseRed
import com.minimalledger.app.ui.components.TransactionTypeToggle
import com.minimalledger.app.ui.screens.AssetScreen
import com.minimalledger.app.ui.screens.EntryScreen
import com.minimalledger.app.ui.screens.LedgerListScreen
import com.minimalledger.app.ui.screens.StatsScreen
import com.minimalledger.app.utils.formatAmountInput
import com.minimalledger.app.utils.formatCurrency
import com.minimalledger.app.utils.parseAmountInputToCents
import com.minimalledger.app.utils.sanitizeAmountInput
import com.minimalledger.app.utils.transactionsToCsv
import com.minimalledger.app.viewmodel.LedgerViewModel
import java.time.LocalDate

private enum class TopLevelDestination(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Entry("\u8BB0\u8D26", Icons.Rounded.AddCircle),
    List("\u8D26\u5355", Icons.AutoMirrored.Rounded.ReceiptLong),
    Stats("\u7EDF\u8BA1", Icons.Rounded.BarChart),
    Assets("资产", Icons.Rounded.AccountBalanceWallet),
}

@Composable
fun LedgerApp(
    repository: TransactionRepository,
    assetRepository: AssetRepository,
    budgetRepository: BudgetRepository,
    modifier: Modifier = Modifier,
) {
    val viewModel: LedgerViewModel = viewModel(
        factory = LedgerViewModel.provideFactory(repository, assetRepository, budgetRepository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedDestination by rememberSaveable { mutableStateOf(TopLevelDestination.Entry) }
    var isAddingTransaction by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var deletingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var editAmountInput by remember { mutableStateOf("") }
    var editType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var editNote by remember { mutableStateOf("") }
    var editAssetAccountId by remember { mutableStateOf<Long?>(null) }
    var isAddingAsset by remember { mutableStateOf(false) }
    var editingAsset by remember { mutableStateOf<AssetAccount?>(null) }
    var deletingAsset by remember { mutableStateOf<AssetAccount?>(null) }
    var assetNameInput by remember { mutableStateOf("") }
    var assetBalanceInput by remember { mutableStateOf("") }
    var assetTypeInput by remember { mutableStateOf(AssetType.CASH) }
    var pendingCsv by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        val csv = pendingCsv
        pendingCsv = null
        if (uri != null && csv != null) {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(csv.toByteArray(Charsets.UTF_8))
            }
            Toast.makeText(context, "\u5DF2\u5BFC\u51FA CSV", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.dismissMessage()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedDestination == destination,
                        onClick = { selectedDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                            )
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (selectedDestination) {
            TopLevelDestination.Entry -> EntryScreen(
                uiState = uiState,
                onAmountChanged = viewModel::updateAmountInput,
                onClearAmount = viewModel::clearAmount,
                onDeleteLastDigit = viewModel::deleteLastDigit,
                onTypeSelected = viewModel::selectType,
                onNoteChanged = viewModel::updateNote,
                onAssetSelected = viewModel::selectAssetAccount,
                onSuggestionClicked = viewModel::applySuggestedNote,
                onQuickRecordClicked = viewModel::applyQuickRecord,
                onSaveClicked = viewModel::saveTransaction,
                modifier = Modifier.padding(innerPadding),
            )

            TopLevelDestination.List -> LedgerListScreen(
                groups = uiState.dayGroups,
                assets = uiState.assets,
                selectedMonth = uiState.selectedLedgerMonth,
                isCurrentMonth = uiState.isCurrentLedgerMonth,
                searchQuery = uiState.ledgerSearchQuery,
                typeFilter = uiState.ledgerTypeFilter,
                onSearchQueryChanged = viewModel::updateLedgerSearchQuery,
                onClearSearchQuery = viewModel::clearLedgerSearchQuery,
                onTypeFilterChanged = viewModel::updateLedgerTypeFilter,
                onAddTransaction = {
                    isAddingTransaction = true
                    editingTransaction = null
                    editAmountInput = ""
                    editType = TransactionType.EXPENSE
                    editNote = ""
                    editAssetAccountId = uiState.assets.firstOrNull()?.id
                },
                onDeleteTransaction = { transaction ->
                    deletingTransaction = transaction
                },
                onEditTransaction = { transaction ->
                    isAddingTransaction = false
                    editingTransaction = transaction
                    editAmountInput = formatAmountInput(transaction.amountInCents)
                    editType = transaction.type
                    editNote = transaction.note
                    editAssetAccountId = transaction.assetAccountId ?: uiState.assets.firstOrNull()?.id
                },
                onShowAllRecords = viewModel::showAllLedgerRecords,
                onShowCurrentMonth = viewModel::showCurrentLedgerMonth,
                onPreviousMonth = viewModel::showPreviousLedgerMonth,
                onNextMonth = viewModel::showNextLedgerMonth,
                modifier = Modifier.padding(innerPadding),
            )

            TopLevelDestination.Stats -> StatsScreen(
                selectedMonth = uiState.selectedStatsMonth,
                isCurrentMonth = uiState.isCurrentStatsMonth,
                summary = uiState.monthlySummary,
                currentAssetInCents = uiState.assetSummary.assetInCents,
                largestExpense = uiState.largestExpense,
                weeklyTrend = uiState.weeklyTrend,
                onPreviousMonth = viewModel::showPreviousStatsMonth,
                onNextMonth = viewModel::showNextStatsMonth,
                onCurrentMonth = viewModel::showCurrentStatsMonth,
                onExportCsv = {
                    pendingCsv = transactionsToCsv(uiState.transactions)
                    exportLauncher.launch("minimal-ledger-${LocalDate.now()}.csv")
                },
                modifier = Modifier.padding(innerPadding),
            )

            TopLevelDestination.Assets -> AssetScreen(
                assets = uiState.assets,
                summary = uiState.assetSummary,
                onAddAsset = {
                    isAddingAsset = true
                    editingAsset = null
                    assetNameInput = ""
                    assetBalanceInput = ""
                    assetTypeInput = AssetType.CASH
                },
                onEditAsset = { asset ->
                    isAddingAsset = false
                    editingAsset = asset
                    assetNameInput = asset.name
                    assetBalanceInput = formatAmountInput(asset.balanceInCents)
                    assetTypeInput = asset.type
                },
                onDeleteAsset = { asset ->
                    deletingAsset = asset
                },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    if (isAddingTransaction) {
        TransactionEditorDialog(
            title = "\u65B0\u589E\u8D26\u5355",
            amountInput = editAmountInput,
            type = editType,
            note = editNote,
            assets = uiState.assets,
            selectedAssetAccountId = editAssetAccountId,
            onAmountChanged = { editAmountInput = sanitizeAmountInput(it) },
            onTypeSelected = { editType = it },
            onNoteChanged = { editNote = it.take(20) },
            onAssetSelected = { editAssetAccountId = it },
            onDismiss = { isAddingTransaction = false },
            onSave = {
                viewModel.createTransaction(
                    amountInput = editAmountInput,
                    type = editType,
                    note = editNote,
                    assetAccountId = editAssetAccountId ?: uiState.assets.firstOrNull()?.id,
                )
                isAddingTransaction = false
            },
        )
    }

    editingTransaction?.let { transaction ->
        TransactionEditorDialog(
            title = "\u7F16\u8F91\u8BB0\u5F55",
            amountInput = editAmountInput,
            type = editType,
            note = editNote,
            assets = uiState.assets,
            selectedAssetAccountId = editAssetAccountId,
            onAmountChanged = { editAmountInput = sanitizeAmountInput(it) },
            onTypeSelected = { editType = it },
            onNoteChanged = { editNote = it.take(20) },
            onAssetSelected = { editAssetAccountId = it },
            onDismiss = { editingTransaction = null },
            onSave = {
                viewModel.updateTransaction(
                    transaction = transaction,
                    amountInput = editAmountInput,
                    type = editType,
                    note = editNote,
                    assetAccountId = editAssetAccountId ?: uiState.assets.firstOrNull()?.id,
                )
                editingTransaction = null
            },
        )
    }

    deletingTransaction?.let { transaction ->
        DeleteConfirmDialog(
            transaction = transaction,
            onDismiss = { deletingTransaction = null },
            onConfirm = {
                viewModel.deleteTransaction(transaction)
                deletingTransaction = null
            },
        )
    }

    if (isAddingAsset) {
        CleanAssetEditorDialog(
            title = "新增资产账户",
            name = assetNameInput,
            type = assetTypeInput,
            balanceInput = assetBalanceInput,
            onNameChanged = { assetNameInput = it.take(20) },
            onTypeSelected = { assetTypeInput = it },
            onBalanceChanged = { assetBalanceInput = sanitizeAmountInput(it) },
            onDismiss = { isAddingAsset = false },
            onSave = {
                viewModel.createAsset(
                    name = assetNameInput,
                    type = assetTypeInput,
                    balanceInput = assetBalanceInput,
                )
                isAddingAsset = false
            },
        )
    }

    editingAsset?.let { asset ->
        CleanAssetEditorDialog(
            isEditing = true,
            title = "编辑资产账户",
            name = assetNameInput,
            type = assetTypeInput,
            balanceInput = assetBalanceInput,
            onNameChanged = { assetNameInput = it.take(20) },
            onTypeSelected = { assetTypeInput = it },
            onBalanceChanged = { assetBalanceInput = sanitizeAmountInput(it) },
            onDismiss = { editingAsset = null },
            onSave = {
                viewModel.updateAsset(
                    asset = asset,
                    name = assetNameInput,
                    type = assetTypeInput,
                    balanceInput = assetBalanceInput,
                )
                editingAsset = null
            },
        )
    }

    deletingAsset?.let { asset ->
        CleanDeleteAssetConfirmDialog(
            asset = asset,
            onDismiss = { deletingAsset = null },
            onConfirm = {
                viewModel.deleteAsset(asset)
                deletingAsset = null
            },
        )
    }

}

@Composable
private fun AssetEditorDialog(
    title: String,
    name: String,
    type: AssetType,
    balanceInput: String,
    onNameChanged: (String) -> Unit,
    onTypeSelected: (AssetType) -> Unit,
    onBalanceChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    label = { Text("账户名称") },
                    placeholder = { Text("例如：招商银行卡、支付宝、信用卡") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = balanceInput,
                    onValueChange = onBalanceChanged,
                    label = { Text("当前余额") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                AssetTypeSelector(
                    selectedType = type,
                    onSelected = onTypeSelected,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = name.isNotBlank() && parseAmountInputToCents(balanceInput) != null,
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun AssetTypeSelector(
    selectedType: AssetType,
    onSelected: (AssetType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "账户类型",
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
        )
        AssetType.entries.chunked(2).forEach { rowTypes ->
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowTypes.forEach { type ->
                    val selected = type == selectedType
                    if (selected) {
                        Button(
                            onClick = { onSelected(type) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(type.label)
                        }
                    } else {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { onSelected(type) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(type.label)
                        }
                    }
                }
                if (rowTypes.size == 1) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DeleteAssetConfirmDialog(
    asset: AssetAccount,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认删除资产账户？") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("账户：${asset.name}")
                Text("余额：${formatCurrency(asset.balanceInCents)}")
                Text("删除后不影响已记录的账单流水。")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun CleanAssetEditorDialog(
    title: String,
    isEditing: Boolean = false,
    name: String,
    type: AssetType,
    balanceInput: String,
    onNameChanged: (String) -> Unit,
    onTypeSelected: (AssetType) -> Unit,
    onBalanceChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEditing) {
                    "\u7F16\u8F91\u8D44\u4EA7\u8D26\u6237"
                } else {
                    "\u65B0\u589E\u8D44\u4EA7\u8D26\u6237"
                },
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    label = { Text("\u8D26\u6237\u540D\u79F0") },
                    placeholder = { Text("\u4F8B\u5982\uFF1A\u62DB\u5546\u94F6\u884C\u5361\u3001\u652F\u4ED8\u5B9D\u3001\u4FE1\u7528\u5361") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = balanceInput,
                    onValueChange = onBalanceChanged,
                    label = { Text("\u5F53\u524D\u4F59\u989D") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                CleanAssetTypeSelector(
                    selectedType = type,
                    onSelected = onTypeSelected,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = name.isNotBlank() && parseAmountInputToCents(balanceInput) != null,
            ) {
                Text("\u4FDD\u5B58")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("\u53D6\u6D88")
            }
        },
    )
}

@Composable
private fun CleanAssetTypeSelector(
    selectedType: AssetType,
    onSelected: (AssetType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "\u8D26\u6237\u7C7B\u578B",
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
        )
        AssetType.entries.chunked(2).forEach { rowTypes ->
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowTypes.forEach { type ->
                    val selected = type == selectedType
                    if (selected) {
                        Button(
                            onClick = { onSelected(type) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(type.label)
                        }
                    } else {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { onSelected(type) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(type.label)
                        }
                    }
                }
                if (rowTypes.size == 1) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CleanDeleteAssetConfirmDialog(
    asset: AssetAccount,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("\u786E\u8BA4\u5220\u9664\u8D44\u4EA7\u8D26\u6237\uFF1F") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("\u8D26\u6237\uFF1A${asset.name}")
                Text("\u4F59\u989D\uFF1A${formatCurrency(asset.balanceInCents)}")
                Text("\u5DF2\u5173\u8054\u8D26\u5355\u7684\u8D44\u4EA7\u4E0D\u4F1A\u88AB\u5220\u9664\uFF0C\u8FD9\u6837\u53EF\u4EE5\u4FDD\u6301\u8D26\u5355\u548C\u8D44\u4EA7\u7684\u5173\u7CFB\u5B8C\u6574\u3002")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
            ) {
                Text("\u5220\u9664")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("\u53D6\u6D88")
            }
        },
    )
}

@Composable
private fun DeleteConfirmDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("\u786E\u8BA4\u5220\u9664\u8FD9\u7B14\u8D26\u5355\uFF1F") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("\u91D1\u989D\uFF1A${formatCurrency(transaction.amountInCents)}")
                Text("\u5907\u6CE8\uFF1A${transaction.note.ifBlank { "\u672A\u586B\u5907\u6CE8" }}")
                Text("\u5220\u9664\u540E\u4E0D\u53EF\u6062\u590D\uFF0C\u8BF7\u518D\u786E\u8BA4\u4E00\u4E0B\u3002")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
            ) {
                Text("\u5220\u9664")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("\u53D6\u6D88")
            }
        },
    )
}

@Composable
private fun TransactionEditorDialog(
    title: String,
    amountInput: String,
    type: TransactionType,
    note: String,
    assets: List<AssetAccount>,
    selectedAssetAccountId: Long?,
    onAmountChanged: (String) -> Unit,
    onTypeSelected: (TransactionType) -> Unit,
    onNoteChanged: (String) -> Unit,
    onAssetSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = onAmountChanged,
                    label = { Text("\u91D1\u989D") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                TransactionTypeToggle(
                    selectedType = type,
                    onSelected = onTypeSelected,
                )
                AssetSelector(
                    assets = assets,
                    selectedAssetAccountId = selectedAssetAccountId,
                    type = type,
                    onAssetSelected = onAssetSelected,
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChanged,
                    label = { Text("\u5907\u6CE8") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = parseAmountInputToCents(amountInput) != null && assets.isNotEmpty(),
            ) {
                Text("\u4FDD\u5B58")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("\u53D6\u6D88")
            }
        },
    )
}

@Composable
private fun AssetSelector(
    assets: List<AssetAccount>,
    selectedAssetAccountId: Long?,
    type: TransactionType,
    onAssetSelected: (Long?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val effectiveSelectedAssetId = selectedAssetAccountId ?: assets.firstOrNull()?.id
        Text(
            text = if (type == TransactionType.INCOME) {
                "\u6536\u5165\u5230"
            } else {
                "\u652F\u51FA\u6765\u6E90"
            },
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
        )
        if (assets.isEmpty()) {
            Text("\u8BF7\u5148\u6DFB\u52A0\u8D44\u4EA7\u8D26\u6237\uFF0C\u8D26\u5355\u9700\u8981\u7ED1\u5B9A\u5BF9\u5E94\u8D44\u4EA7")
            return
        }
        assets.chunked(2).forEach { rowAssets ->
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowAssets.forEach { asset ->
                    val selected = asset.id == effectiveSelectedAssetId
                    if (selected) {
                        Button(
                            onClick = { onAssetSelected(asset.id) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(asset.name.take(6))
                        }
                    } else {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { onAssetSelected(asset.id) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(asset.name.take(6))
                        }
                    }
                }
                if (rowAssets.size == 1) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
