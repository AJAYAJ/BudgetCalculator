package com.vegam.budgetcalculator.data.repository

import com.vegam.budgetcalculator.data.local.dao.MonthlyBudgetDao
import com.vegam.budgetcalculator.data.mapper.toDomain
import com.vegam.budgetcalculator.data.mapper.toEntity
import com.vegam.budgetcalculator.domain.model.MonthlyBudget
import com.vegam.budgetcalculator.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: MonthlyBudgetDao
) : BudgetRepository {

    override fun observeMonthlyBudgets(userId: String, year: Int, month: Int): Flow<List<MonthlyBudget>> {
        return budgetDao.observeMonthlyBudgets(userId, year, month).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getMonthlyBudgetsList(userId: String, year: Int, month: Int): List<MonthlyBudget> {
        return budgetDao.getMonthlyBudgetsList(userId, year, month).map { it.toDomain() }
    }

    override suspend fun setMonthlyBudget(budget: MonthlyBudget) {
        budgetDao.insertMonthlyBudget(budget.toEntity())
    }

    override suspend fun setMonthlyBudgets(budgets: List<MonthlyBudget>) {
        budgetDao.insertMonthlyBudgets(budgets.map { it.toEntity() })
    }
}
