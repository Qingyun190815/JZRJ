package com.minimalledger.app.data.repository

import com.minimalledger.app.data.local.AssetDao
import com.minimalledger.app.data.local.AssetEntity
import com.minimalledger.app.data.model.AssetAccount
import com.minimalledger.app.data.model.AssetType
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineAssetRepository(
    private val assetDao: AssetDao,
) : AssetRepository {
    override fun observeAssets(): Flow<List<AssetAccount>> {
        return assetDao.observeAssets().map { assets ->
            assets.map { it.asExternalModel() }
        }
    }

    override suspend fun addAsset(asset: AssetAccount) {
        assetDao.insert(asset.asEntity())
    }

    override suspend fun updateAsset(asset: AssetAccount) {
        assetDao.update(asset.asEntity())
    }

    override suspend fun deleteAsset(asset: AssetAccount) {
        assetDao.delete(asset.asEntity())
    }
}

private fun AssetEntity.asExternalModel(): AssetAccount {
    return AssetAccount(
        id = id,
        name = name,
        type = AssetType.valueOf(type),
        balanceInCents = balanceInCents,
        updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
    )
}

private fun AssetAccount.asEntity(): AssetEntity {
    return AssetEntity(
        id = id,
        name = name,
        type = type.name,
        balanceInCents = balanceInCents,
        updatedAtEpochMillis = updatedAt.toEpochMilli(),
    )
}
