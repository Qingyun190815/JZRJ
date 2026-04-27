package com.minimalledger.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_budgets")
data class BudgetEntity(
    @PrimaryKey
    val month: String,
    val amountInCents: Long,
    val updatedAtEpochMillis: Long,
)
