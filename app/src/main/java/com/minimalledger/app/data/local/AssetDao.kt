package com.minimalledger.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query(
        """
        SELECT * FROM asset_accounts
        ORDER BY updatedAtEpochMillis DESC
        """,
    )
    fun observeAssets(): Flow<List<AssetEntity>>

    @Insert
    suspend fun insert(asset: AssetEntity)

    @Update
    suspend fun update(asset: AssetEntity)

    @Delete
    suspend fun delete(asset: AssetEntity)
}
