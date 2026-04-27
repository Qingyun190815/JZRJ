package com.minimalledger.app.data.model

import java.time.Instant

data class AssetAccount(
    val id: Long = 0L,
    val name: String,
    val type: AssetType,
    val balanceInCents: Long,
    val updatedAt: Instant,
)
