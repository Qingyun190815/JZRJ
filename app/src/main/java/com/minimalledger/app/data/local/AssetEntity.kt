package com.minimalledger.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asset_accounts")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val type: String,
    val balanceInCents: Long,
    val updatedAtEpochMillis: Long,
)
