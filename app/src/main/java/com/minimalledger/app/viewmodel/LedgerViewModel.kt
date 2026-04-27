package com.minimalledger.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.minimalledger.app.data.model.AssetAccount
import com.minimalledger.app.data.model.AssetType
import com.minimalledger.app.data.model.DailyTrendPoint
import com.minimalledger.app.data.model.MonthlyBudget
import com.minimalledger.app.data.model.MonthlySummary
import com.minimalledger.app.data.model.NoteSuggestion
import com.minimalledger.app.data.model.Transaction
import com.minimalledger.app.data.model.TransactionType
import com.minimalledger.app.data.repository.AssetRepository
import com.minimalledger.app.data.repository.BudgetRepository
import com.minimalledger.app.data.repository.TransactionRepository
import com.minimalledger.app.utils.parseAmountInputToCents
import com.minimalledger.app.utils.sanitizeAmountInput
import com.minimalledger.app.utils.toLocalDate
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionDayGroup(
    val date: LocalDate,
    val transactions: List<Transaction>,
    val incomeInCents: Long,
    val expenseInCents: Long,
)

data class AssetSummary(
    val assetInCents: Long = 0L,
    val liabilityInCents: Long = 0L,
) {
    val netWorthInCents: Long
        get() = assetInCents - liabilityInCents
}

data class BudgetStatus(
    val month: YearMonth = YearMonth.now(),
    val budgetInCents: Long = 0L,
    val expenseInCents: Long = 0L,
) {
    val isBudgetSet: Boolean
        get() = budgetInCents > 0L

    val remainingInCents: Long
        get() = budgetInCents - expenseInCents

    val usedPercent: Int
        get() = if (budgetInCents <= 0L) 0 else ((expenseInCents.toDouble() / budgetInCents) * 100).toInt()

    val progress: Float
        get() = if (budgetInCents <= 0L) 0f else (expenseInCents.toFloat() / budgetInCents.toFloat()).coerceIn(0f, 1f)
}

enum class LedgerTypeFilter {
    ALL,
    EXPENSE,
    INCOME,
}

private data class EntryDraft(
    val amount: String,
    val type: TransactionType,
    val note: String,
    val assetAccountId: Long?,
    val message: String?,
)

private data class LedgerFilter(
    val month: YearMonth?,
    val query: String,
    val typeFilter: LedgerTypeFilter,
)

private data class LedgerData(
    val transactions: List<Transaction>,
    val suggestions: List<NoteSuggestion>,
    val assets: List<AssetAccount>,
    val budgets: List<MonthlyBudget>,
)

data class LedgerUiState(
    val amountInput: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val noteInput: String = "",
    val selectedAssetAccountId: Long? = null,
    val noteSuggestions: List<NoteSuggestion> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val selectedLedgerMonth: YearMonth? = null,
    val ledgerSearchQuery: String = "",
    val ledgerTypeFilter: LedgerTypeFilter = LedgerTypeFilter.ALL,
    val dayGroups: List<TransactionDayGroup> = emptyList(),
    val selectedStatsMonth: YearMonth = YearMonth.now(),
    val monthlySummary: MonthlySummary = MonthlySummary(),
    val largestExpense: Transaction? = null,
    val weeklyTrend: List<DailyTrendPoint> = emptyList(),
    val assets: List<AssetAccount> = emptyList(),
    val assetSummary: AssetSummary = AssetSummary(),
    val budgetStatus: BudgetStatus = BudgetStatus(),
    val statusMessage: String? = null,
) {
    val canSave: Boolean
        get() = parseAmountInputToCents(amountInput) != null && assets.isNotEmpty()

    val isCurrentStatsMonth: Boolean
        get() = selectedStatsMonth == YearMonth.now()

    val isCurrentLedgerMonth: Boolean
        get() = selectedLedgerMonth == YearMonth.now()
}

class LedgerViewModel(
    private val repository: TransactionRepository,
    private val assetRepository: AssetRepository,
    private val budgetRepository: BudgetRepository,
) : ViewModel() {
    private val amountInput = MutableStateFlow("")
    private val selectedType = MutableStateFlow(TransactionType.EXPENSE)
    private val noteInput = MutableStateFlow("")
    private val selectedAssetAccountId = MutableStateFlow<Long?>(null)
    private val statusMessage = MutableStateFlow<String?>(null)
    private val zoneId = ZoneId.systemDefault()
    private val selectedLedgerMonth = MutableStateFlow<YearMonth?>(null)
    private val ledgerSearchQuery = MutableStateFlow("")
    private val ledgerTypeFilter = MutableStateFlow(LedgerTypeFilter.ALL)
    private val selectedStatsMonth = MutableStateFlow(YearMonth.now(zoneId))

    private val entryDraft = combine(
        amountInput,
        selectedType,
        noteInput,
        selectedAssetAccountId,
        statusMessage,
    ) { amount, type, note, assetAccountId, message ->
        EntryDraft(
            amount = amount,
            type = type,
            note = note,
            assetAccountId = assetAccountId,
            message = message,
        )
    }

    private val ledgerFilter = combine(
        selectedLedgerMonth,
        ledgerSearchQuery,
        ledgerTypeFilter,
    ) { month, query, typeFilter ->
        LedgerFilter(
            month = month,
            query = query,
            typeFilter = typeFilter,
        )
    }

    val uiState: StateFlow<LedgerUiState> = combine(
        entryDraft,
        selectedStatsMonth,
        ledgerFilter,
        combine(
            repository.observeTransactions(),
            repository.observeCommonNotes(),
            assetRepository.observeAssets(),
            budgetRepository.observeBudgets(),
        ) { transactions, suggestions, assets, budgets ->
            LedgerData(
                transactions = transactions,
                suggestions = suggestions,
                assets = assets,
                budgets = budgets,
            )
        },
    ) { draft, statsMonth, filter, ledgerData ->
        val transactions = ledgerData.transactions
        val suggestions = ledgerData.suggestions
        val assets = ledgerData.assets
        val assetSummary = assets.toAssetSummary()
        val monthlySummary = transactions.toMonthlySummary(zoneId, statsMonth)
        val monthTransactions = filter.month?.let {
            transactions.filterByMonth(zoneId, it)
        } ?: transactions
        val ledgerTransactions = monthTransactions
            .filterByType(filter.typeFilter)
            .filterByQuery(filter.query, zoneId)

        LedgerUiState(
            amountInput = draft.amount,
            selectedType = draft.type,
            noteInput = draft.note,
            selectedAssetAccountId = draft.assetAccountId ?: assets.firstOrNull()?.id,
            noteSuggestions = suggestions,
            transactions = transactions,
            selectedLedgerMonth = filter.month,
            ledgerSearchQuery = filter.query,
            ledgerTypeFilter = filter.typeFilter,
            dayGroups = ledgerTransactions.toDayGroups(zoneId),
            selectedStatsMonth = statsMonth,
            monthlySummary = monthlySummary,
            largestExpense = transactions.largestExpenseInMonth(zoneId, statsMonth),
            weeklyTrend = transactions.toWeeklyTrend(zoneId),
            assets = assets,
            assetSummary = assetSummary,
            budgetStatus = BudgetStatus(
                month = statsMonth,
                budgetInCents = assetSummary.assetInCents + monthlySummary.expenseInCents,
                expenseInCents = monthlySummary.expenseInCents,
            ),
            statusMessage = draft.message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LedgerUiState(),
    )

    fun inputKey(value: String) {
        amountInput.update { current ->
            when (value) {
                "." -> addDecimalPoint(current)
                else -> appendDigit(current, value)
            }
        }
    }

    fun deleteLastDigit() {
        amountInput.update { current ->
            if (current.isEmpty()) "" else current.dropLast(1)
        }
    }

    fun clearAmount() {
        amountInput.value = ""
    }

    fun updateAmountInput(input: String) {
        amountInput.value = sanitizeAmountInput(input)
    }

    fun selectType(type: TransactionType) {
        selectedType.value = type
    }

    fun updateNote(note: String) {
        noteInput.value = note.take(20)
    }

    fun selectAssetAccount(assetAccountId: Long?) {
        selectedAssetAccountId.value = assetAccountId
    }

    fun applySuggestedNote(note: String) {
        noteInput.value = note
    }

    fun applyQuickRecord(
        amountInCents: Long,
        type: TransactionType,
        note: String,
    ) {
        amountInput.value = amountInCents.toAmountInput()
        selectedType.value = type
        noteInput.value = note.take(20)
    }

    fun dismissMessage() {
        statusMessage.value = null
    }

    fun showPreviousStatsMonth() {
        selectedStatsMonth.update { it.minusMonths(1) }
    }

    fun showNextStatsMonth() {
        selectedStatsMonth.update { current ->
            val now = YearMonth.now(zoneId)
            if (current < now) current.plusMonths(1) else current
        }
    }

    fun showCurrentStatsMonth() {
        selectedStatsMonth.value = YearMonth.now(zoneId)
    }

    fun showAllLedgerRecords() {
        selectedLedgerMonth.value = null
    }

    fun showCurrentLedgerMonth() {
        selectedLedgerMonth.value = YearMonth.now(zoneId)
    }

    fun showPreviousLedgerMonth() {
        selectedLedgerMonth.update { current ->
            (current ?: YearMonth.now(zoneId)).minusMonths(1)
        }
    }

    fun showNextLedgerMonth() {
        selectedLedgerMonth.update { current ->
            val now = YearMonth.now(zoneId)
            when {
                current == null -> now
                current < now -> current.plusMonths(1)
                else -> current
            }
        }
    }

    fun updateLedgerSearchQuery(query: String) {
        ledgerSearchQuery.value = query.take(30)
    }

    fun clearLedgerSearchQuery() {
        ledgerSearchQuery.value = ""
    }

    fun updateLedgerTypeFilter(filter: LedgerTypeFilter) {
        ledgerTypeFilter.value = filter
    }

    fun createTransaction(
        amountInput: String,
        type: TransactionType,
        note: String,
        assetAccountId: Long?,
    ) {
        val amountInCents = parseAmountInputToCents(amountInput) ?: return
        val linkedAssetAccountId = assetAccountId ?: uiState.value.assets.firstOrNull()?.id
        if (linkedAssetAccountId == null) {
            statusMessage.value = "\u8BF7\u5148\u6DFB\u52A0\u8D44\u4EA7\u8D26\u6237"
            return
        }
        val now = Instant.now()
        val transaction = Transaction(
            amountInCents = amountInCents,
            type = type,
            note = note.trim().take(20),
            assetAccountId = linkedAssetAccountId,
            timestamp = now,
            createdAt = now,
            updatedAt = now,
        )
        viewModelScope.launch {
            repository.addTransaction(transaction)
            applyTransactionAssetEffect(transaction)
            statusMessage.value = "\u8D26\u5355\u5DF2\u65B0\u589E"
        }
    }

    fun saveTransaction() {
        val amountInCents = parseAmountInputToCents(amountInput.value) ?: return
        val linkedAssetAccountId = selectedAssetAccountId.value ?: uiState.value.assets.firstOrNull()?.id
        if (linkedAssetAccountId == null) {
            statusMessage.value = "\u8BF7\u5148\u6DFB\u52A0\u8D44\u4EA7\u8D26\u6237"
            return
        }
        val now = Instant.now()
        val transaction = Transaction(
            amountInCents = amountInCents,
            type = selectedType.value,
            note = noteInput.value.trim(),
            assetAccountId = linkedAssetAccountId,
            timestamp = now,
            createdAt = now,
            updatedAt = now,
        )

        viewModelScope.launch {
            repository.addTransaction(transaction)
            applyTransactionAssetEffect(transaction)
            amountInput.value = ""
            noteInput.value = ""
            statusMessage.value = if (transaction.type == TransactionType.INCOME) {
                "\u6536\u5165\u5DF2\u8BB0\u5F55"
            } else {
                "\u652F\u51FA\u5DF2\u8BB0\u5F55"
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            reverseTransactionAssetEffect(transaction)
            repository.deleteTransaction(transaction)
            statusMessage.value = "\u8BB0\u5F55\u5DF2\u5220\u9664"
        }
    }

    fun updateTransaction(
        transaction: Transaction,
        amountInput: String,
        type: TransactionType,
        note: String,
        assetAccountId: Long?,
    ) {
        val amountInCents = parseAmountInputToCents(amountInput) ?: return
        val linkedAssetAccountId = assetAccountId ?: uiState.value.assets.firstOrNull()?.id
        if (linkedAssetAccountId == null) {
            statusMessage.value = "\u8BF7\u5148\u6DFB\u52A0\u8D44\u4EA7\u8D26\u6237"
            return
        }
        val updatedTransaction = transaction.copy(
            amountInCents = amountInCents,
            type = type,
            note = note.trim().take(20),
            assetAccountId = linkedAssetAccountId,
            updatedAt = Instant.now(),
        )
        viewModelScope.launch {
            repository.updateTransaction(updatedTransaction)
            applyTransactionAssetChanges(
                oldTransaction = transaction,
                newTransaction = updatedTransaction,
            )
            statusMessage.value = "\u8BB0\u5F55\u5DF2\u66F4\u65B0"
        }
    }

    fun createAsset(
        name: String,
        type: AssetType,
        balanceInput: String,
    ) {
        val balanceInCents = parseAmountInputToCents(balanceInput) ?: return
        val trimmedName = name.trim().take(20)
        if (trimmedName.isBlank()) {
            return
        }
        viewModelScope.launch {
            assetRepository.addAsset(
                AssetAccount(
                    name = trimmedName,
                    type = type,
                    balanceInCents = balanceInCents,
                    updatedAt = Instant.now(),
                ),
            )
            statusMessage.value = "\u8D44\u4EA7\u8D26\u6237\u5DF2\u65B0\u589E"
        }
    }

    fun updateAsset(
        asset: AssetAccount,
        name: String,
        type: AssetType,
        balanceInput: String,
    ) {
        val balanceInCents = parseAmountInputToCents(balanceInput) ?: return
        val trimmedName = name.trim().take(20)
        if (trimmedName.isBlank()) {
            return
        }
        viewModelScope.launch {
            assetRepository.updateAsset(
                asset.copy(
                    name = trimmedName,
                    type = type,
                    balanceInCents = balanceInCents,
                    updatedAt = Instant.now(),
                ),
            )
            statusMessage.value = "\u8D44\u4EA7\u8D26\u6237\u5DF2\u66F4\u65B0"
        }
    }

    fun deleteAsset(asset: AssetAccount) {
        if (uiState.value.transactions.any { it.assetAccountId == asset.id }) {
            statusMessage.value = "\u8BE5\u8D44\u4EA7\u5DF2\u5173\u8054\u8D26\u5355\uFF0C\u8BF7\u5148\u8C03\u6574\u8BB0\u5F55"
            return
        }
        viewModelScope.launch {
            assetRepository.deleteAsset(asset)
            statusMessage.value = "\u8D44\u4EA7\u8D26\u6237\u5DF2\u5220\u9664"
        }
    }

    private suspend fun applyTransactionAssetEffect(transaction: Transaction) {
        adjustLinkedAsset(transaction, multiplier = 1)
    }

    private suspend fun reverseTransactionAssetEffect(transaction: Transaction) {
        adjustLinkedAsset(transaction, multiplier = -1)
    }

    private suspend fun adjustLinkedAsset(
        transaction: Transaction,
        multiplier: Int,
    ) {
        val assetId = transaction.assetAccountId ?: return
        val asset = uiState.value.assets.firstOrNull { it.id == assetId } ?: return
        val delta = transaction.assetDeltaFor(asset) * multiplier
        assetRepository.updateAsset(
            asset.copy(
                balanceInCents = asset.balanceInCents + delta,
                updatedAt = Instant.now(),
            ),
        )
    }

    private suspend fun applyTransactionAssetChanges(
        oldTransaction: Transaction,
        newTransaction: Transaction,
    ) {
        val assetsById = uiState.value.assets.associateBy { it.id }
        val deltas = mutableMapOf<Long, Long>()

        oldTransaction.assetAccountId?.let { assetId ->
            val asset = assetsById[assetId] ?: return@let
            deltas[assetId] = (deltas[assetId] ?: 0L) - oldTransaction.assetDeltaFor(asset)
        }
        newTransaction.assetAccountId?.let { assetId ->
            val asset = assetsById[assetId] ?: return@let
            deltas[assetId] = (deltas[assetId] ?: 0L) + newTransaction.assetDeltaFor(asset)
        }

        deltas.forEach { (assetId, delta) ->
            val asset = assetsById[assetId] ?: return@forEach
            assetRepository.updateAsset(
                asset.copy(
                    balanceInCents = asset.balanceInCents + delta,
                    updatedAt = Instant.now(),
                ),
            )
        }
    }

    companion object {
        fun provideFactory(
            repository: TransactionRepository,
            assetRepository: AssetRepository,
            budgetRepository: BudgetRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LedgerViewModel(repository, assetRepository, budgetRepository) as T
                }
            }
        }
    }
}

private fun appendDigit(current: String, digit: String): String {
    if (digit !in "0".."9") {
        return current
    }
    val candidate = current + digit
    return enforceDecimalPrecision(candidate)
}

private fun addDecimalPoint(current: String): String {
    if (current.isEmpty()) {
        return "0."
    }
    if (current.contains('.')) {
        return current
    }
    return "$current."
}

private fun enforceDecimalPrecision(value: String): String {
    val parts = value.split('.')
    return if (parts.size == 2 && parts[1].length > 2) {
        value.dropLast(1)
    } else {
        value
    }
}

private fun Long.toAmountInput(): String {
    val yuan = this / 100
    val fen = kotlin.math.abs(this % 100)
    return if (fen == 0L) {
        yuan.toString()
    } else {
        "$yuan.${fen.toString().padStart(2, '0')}"
    }
}

private fun List<Transaction>.toDayGroups(zoneId: ZoneId): List<TransactionDayGroup> {
    return groupBy { it.timestamp.toLocalDate(zoneId) }
        .toSortedMap(compareByDescending { it })
        .map { (date, transactions) ->
            TransactionDayGroup(
                date = date,
                transactions = transactions.sortedByDescending { it.timestamp },
                incomeInCents = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amountInCents },
                expenseInCents = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amountInCents },
            )
        }
}

private fun List<Transaction>.toMonthlySummary(
    zoneId: ZoneId,
    month: YearMonth,
): MonthlySummary {
    val monthTransactions = filterByMonth(zoneId, month)
    return MonthlySummary(
        incomeInCents = monthTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amountInCents },
        expenseInCents = monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amountInCents },
    )
}

private fun List<Transaction>.largestExpenseInMonth(
    zoneId: ZoneId,
    month: YearMonth,
): Transaction? {
    return filterByMonth(zoneId, month)
        .filter { it.type == TransactionType.EXPENSE }
        .maxByOrNull { it.amountInCents }
}

private fun List<Transaction>.filterByMonth(
    zoneId: ZoneId,
    month: YearMonth,
): List<Transaction> {
    return filter { YearMonth.from(it.timestamp.atZone(zoneId)) == month }
}

private fun List<Transaction>.filterByType(filter: LedgerTypeFilter): List<Transaction> {
    return when (filter) {
        LedgerTypeFilter.ALL -> this
        LedgerTypeFilter.EXPENSE -> filter { it.type == TransactionType.EXPENSE }
        LedgerTypeFilter.INCOME -> filter { it.type == TransactionType.INCOME }
    }
}

private fun List<Transaction>.filterByQuery(
    query: String,
    zoneId: ZoneId,
): List<Transaction> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) {
        return this
    }
    return filter { transaction ->
        transaction.note.contains(normalizedQuery, ignoreCase = true) ||
            transaction.amountInCents.toString().contains(normalizedQuery) ||
            transaction.amountInCents.toAmountInput().contains(normalizedQuery) ||
            transaction.type.name.contains(normalizedQuery, ignoreCase = true) ||
            transaction.type.searchLabels().any { it.contains(normalizedQuery, ignoreCase = true) } ||
            transaction.dateSearchLabels(zoneId).any { it.contains(normalizedQuery, ignoreCase = true) }
    }
}

private fun Transaction.dateSearchLabels(zoneId: ZoneId): List<String> {
    val date = timestamp.toLocalDate(zoneId)
    return listOf(
        date.toString(),
        "${date.monthValue}/${date.dayOfMonth}",
        "${date.monthValue}\u6708${date.dayOfMonth}\u65E5",
        "${date.year}\u5E74${date.monthValue}\u6708",
    )
}

private fun TransactionType.searchLabels(): List<String> {
    return when (this) {
        TransactionType.INCOME -> listOf("\u6536\u5165", "\u8FDB\u8D26", "income")
        TransactionType.EXPENSE -> listOf("\u652F\u51FA", "\u82B1\u8D39", "\u6D88\u8D39", "expense")
    }
}

private fun Transaction.assetDeltaFor(asset: AssetAccount): Long {
    return if (asset.type.isLiability) {
        when (type) {
            TransactionType.EXPENSE -> amountInCents
            TransactionType.INCOME -> -amountInCents
        }
    } else {
        when (type) {
            TransactionType.EXPENSE -> -amountInCents
            TransactionType.INCOME -> amountInCents
        }
    }
}

private fun List<AssetAccount>.toAssetSummary(): AssetSummary {
    return AssetSummary(
        assetInCents = filter { !it.type.isLiability }.sumOf { it.balanceInCents },
        liabilityInCents = filter { it.type.isLiability }.sumOf { it.balanceInCents },
    )
}

private fun List<Transaction>.toWeeklyTrend(zoneId: ZoneId): List<DailyTrendPoint> {
    val today = LocalDate.now(zoneId)
    val range = (6L downTo 0L).map { today.minusDays(it) }
    return range.map { date ->
        val items = filter { it.timestamp.toLocalDate(zoneId) == date }
        DailyTrendPoint(
            date = date,
            incomeInCents = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amountInCents },
            expenseInCents = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountInCents },
        )
    }
}
