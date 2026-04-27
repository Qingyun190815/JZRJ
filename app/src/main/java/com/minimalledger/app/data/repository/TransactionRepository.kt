package com.minimalledger.app.data.repository

import com.minimalledger.app.data.model.NoteSuggestion
import com.minimalledger.app.data.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    fun observeCommonNotes(limit: Int = 8): Flow<List<NoteSuggestion>>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
}
