package com.minimalledger.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT * FROM transactions
        ORDER BY timestampEpochMillis DESC
        """,
    )
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query(
        """
        SELECT note, COUNT(*) AS usageCount
        FROM transactions
        WHERE note != ''
        GROUP BY note
        ORDER BY usageCount DESC, MAX(updatedAtEpochMillis) DESC
        LIMIT :limit
        """,
    )
    fun observeCommonNotes(limit: Int = 8): Flow<List<NoteSuggestionRow>>
}
