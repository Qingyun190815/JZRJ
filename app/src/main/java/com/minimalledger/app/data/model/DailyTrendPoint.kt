package com.minimalledger.app.data.model

import java.time.LocalDate

data class DailyTrendPoint(
    val date: LocalDate,
    val incomeInCents: Long,
    val expenseInCents: Long,
)
