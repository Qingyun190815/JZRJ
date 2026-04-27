package com.minimalledger.app.data.model

import java.time.Instant

data class Transaction(
    val id: Long = 0L,
    val amountInCents: Long,
    val type: TransactionType,
    val note: String,
    val assetAccountId: Long? = null,
    val timestamp: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
)
