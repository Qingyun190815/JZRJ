package com.minimalledger.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "amount_in_cents")
    val amountInCents: Long,
    val type: String,
    val note: String,
    val assetAccountId: Long?,
    val timestampEpochMillis: Long,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
