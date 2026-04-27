package com.minimalledger.app.data.repository

import com.minimalledger.app.data.local.BudgetDao
import com.minimalledger.app.data.local.BudgetEntity
import com.minimalledger.app.data.model.MonthlyBudget
import java.time.Instant
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineBudgetRepository(
    private val budgetDao: BudgetDao,
) : BudgetRepository {
    override fun observeBudgets(): Flow<List<MonthlyBudget>> {
        return budgetDao.observeBudgets().map { budgets ->
            budgets.map { it.asExternalModel() }
        }
    }

    override suspend fun saveBudget(budget: MonthlyBudget) {
        budgetDao.upsertBudget(budget.asEntity())
    }
}

private fun BudgetEntity.asExternalModel(): MonthlyBudget {
    return MonthlyBudget(
        month = YearMonth.parse(month),
        amountInCents = amountInCents,
        updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
    )
}

private fun MonthlyBudget.asEntity(): BudgetEntity {
    return BudgetEntity(
        month = month.toString(),
        amountInCents = amountInCents,
        updatedAtEpochMillis = updatedAt.toEpochMilli(),
    )
}
