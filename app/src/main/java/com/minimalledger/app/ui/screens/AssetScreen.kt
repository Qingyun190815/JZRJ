package com.minimalledger.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalledger.app.data.model.AssetAccount
import com.minimalledger.app.ui.theme.ExpenseRed
import com.minimalledger.app.ui.theme.IncomeGreen
import com.minimalledger.app.ui.theme.LedgerAccent
import com.minimalledger.app.ui.theme.LedgerPrimary
import com.minimalledger.app.ui.theme.LedgerSurface
import com.minimalledger.app.utils.formatCurrency
import com.minimalledger.app.viewmodel.AssetSummary

@Composable
fun AssetScreen(
    assets: List<AssetAccount>,
    summary: AssetSummary,
    onAddAsset: () -> Unit,
    onEditAsset: (AssetAccount) -> Unit,
    onDeleteAsset: (AssetAccount) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            AssetHeroCard(summary = summary)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "资产",
                    amount = summary.assetInCents,
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    title = "负债",
                    amount = summary.liabilityInCents,
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Button(
                onClick = onAddAsset,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LedgerPrimary),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                )
                Text("新增资产账户")
            }
        }

        if (assets.isEmpty()) {
            item {
                EmptyAssetCard()
            }
        } else {
            items(assets, key = { it.id }) { asset ->
                AssetAccountRow(
                    asset = asset,
                    onEditAsset = onEditAsset,
                    onDeleteAsset = onDeleteAsset,
                )
            }
        }
    }
}

@Composable
private fun AssetHeroCard(summary: AssetSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = LedgerSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            LedgerPrimary,
                            LedgerAccent,
                        ),
                    ),
                )
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "资产总览",
                style = MaterialTheme.typography.titleMedium,
                color = LedgerSurface.copy(alpha = 0.82f),
            )
            Text(
                text = formatCurrency(summary.netWorthInCents),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = LedgerSurface,
            )
            Text(
                text = "净资产 = 资产 - 负债",
                style = MaterialTheme.typography.bodyMedium,
                color = LedgerSurface.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    amount: Long,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LedgerSurface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
            )
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

@Composable
private fun EmptyAssetCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = LedgerSurface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "还没有资产账户",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "可以添加现金、银行卡、支付宝、微信、信用卡等账户，资产会自动汇总。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
    }
}

@Composable
private fun AssetAccountRow(
    asset: AssetAccount,
    onEditAsset: (AssetAccount) -> Unit,
    onDeleteAsset: (AssetAccount) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditAsset(asset) },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = LedgerSurface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (asset.type.isLiability) ExpenseRed.copy(alpha = 0.14f) else IncomeGreen.copy(alpha = 0.14f),
                        RoundedCornerShape(18.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = asset.type.label.take(2),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (asset.type.isLiability) ExpenseRed else IncomeGreen,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = asset.type.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                )
            }
            Text(
                text = formatCurrency(asset.balanceInCents),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (asset.type.isLiability) ExpenseRed else LedgerPrimary,
            )
            IconButton(onClick = { onDeleteAsset(asset) }) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "删除资产",
                    tint = ExpenseRed,
                )
            }
        }
    }
}
