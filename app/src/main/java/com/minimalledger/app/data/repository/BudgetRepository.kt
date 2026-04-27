package com.minimalledger.app.data.repository

import com.minimalledger.app.data.model.MonthlyBudget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeBudgets(): Flow<List<MonthlyBudget>>
    suspend fun saveBudget(budget: MonthlyBudget)
}
