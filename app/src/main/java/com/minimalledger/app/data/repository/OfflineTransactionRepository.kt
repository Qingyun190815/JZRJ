package com.minimalledger.app.data.repository

import com.minimalledger.app.data.local.TransactionDao
import com.minimalledger.app.data.local.TransactionEntity
import com.minimalledger.app.data.model.NoteSuggestion
import com.minimalledger.app.data.model.Transaction
import com.minimalledger.app.data.model.TransactionType
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineTransactionRepository(
    private val transactionDao: TransactionDao,
) : TransactionRepository {
    override fun observeTransactions(): Flow<List<Transaction>> {
        return transactionDao.observeTransactions().map { items ->
            items.map { it.asExternalModel() }
        }
    }

    override fun observeCommonNotes(limit: Int): Flow<List<NoteSuggestion>> {
        return transactionDao.observeCommonNotes(limit).map { items ->
            items.map { NoteSuggestion(note = it.note, usageCount = it.usageCount) }
        }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insert(transaction.asEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction.asEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction.asEntity())
    }
}

private fun TransactionEntity.asExternalModel(): Transaction {
    return Transaction(
        id = id,
        amountInCents = amountInCents,
        type = TransactionType.valueOf(type),
        note = note,
        assetAccountId = assetAccountId,
        timestamp = Instant.ofEpochMilli(timestampEpochMillis),
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
        updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
    )
}

private fun Transaction.asEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amountInCents = amountInCents,
        type = type.name,
        note = note,
        assetAccountId = assetAccountId,
        timestampEpochMillis = timestamp.toEpochMilli(),
        createdAtEpochMillis = createdAt.toEpochMilli(),
        updatedAtEpochMillis = updatedAt.toEpochMilli(),
    )
}
