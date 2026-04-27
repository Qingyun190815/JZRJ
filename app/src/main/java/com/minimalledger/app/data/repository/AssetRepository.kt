package com.minimalledger.app.data.repository

import com.minimalledger.app.data.model.AssetAccount
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun observeAssets(): Flow<List<AssetAccount>>
    suspend fun addAsset(asset: AssetAccount)
    suspend fun updateAsset(asset: AssetAccount)
    suspend fun deleteAsset(asset: AssetAccount)
}
