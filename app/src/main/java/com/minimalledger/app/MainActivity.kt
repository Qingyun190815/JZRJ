package com.minimalledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.minimalledger.app.data.local.AppDatabase
import com.minimalledger.app.data.repository.OfflineAssetRepository
import com.minimalledger.app.data.repository.OfflineBudgetRepository
import com.minimalledger.app.data.repository.OfflineTransactionRepository
import com.minimalledger.app.ui.LedgerApp
import com.minimalledger.app.ui.theme.MinimalLedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = OfflineTransactionRepository(database.transactionDao())
        val assetRepository = OfflineAssetRepository(database.assetDao())
        val budgetRepository = OfflineBudgetRepository(database.budgetDao())

        setContent {
            MinimalLedgerTheme {
                LedgerApp(
                    repository = repository,
                    assetRepository = assetRepository,
                    budgetRepository = budgetRepository,
                )
            }
        }
    }
}
