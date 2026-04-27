package com.minimalledger.app.data.model

data class MonthlySummary(
    val incomeInCents: Long = 0L,
    val expenseInCents: Long = 0L,
) {
    val balanceInCents: Long
        get() = incomeInCents - expenseInCents
}
