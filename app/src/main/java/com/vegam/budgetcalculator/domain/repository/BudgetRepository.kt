package com.vegam.budgetcalculator.domain.repository

import com.vegam.budgetcalculator.domain.model.MonthlyBudget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeMonthlyBudgets(userId: String, year: Int, month: Int): Flow<List<MonthlyBudget>>
    suspend fun getMonthlyBudgetsList(userId: String, year: Int, month: Int): List<MonthlyBudget>
    suspend fun setMonthlyBudget(budget: MonthlyBudget)
    suspend fun setMonthlyBudgets(budgets: List<MonthlyBudget>)
}
