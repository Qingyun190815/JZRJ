package com.minimalledger.app.data.model

import java.time.Instant
import java.time.YearMonth

data class MonthlyBudget(
    val month: YearMonth,
    val amountInCents: Long,
    val updatedAt: Instant,
)
