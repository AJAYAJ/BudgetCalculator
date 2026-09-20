package com.vegam.budgetcalculator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vegam.budgetcalculator.data.local.entity.MonthlyBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyBudgetDao {
    @Query("SELECT * FROM monthly_budgets WHERE userId = :userId AND year = :year AND month = :month")
    fun observeMonthlyBudgets(userId: String, year: Int, month: Int): Flow<List<MonthlyBudgetEntity>>

    @Query("SELECT * FROM monthly_budgets WHERE userId = :userId AND year = :year AND month = :month")
    suspend fun getMonthlyBudgetsList(userId: String, year: Int, month: Int): List<MonthlyBudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyBudget(budget: MonthlyBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyBudgets(budgets: List<MonthlyBudgetEntity>)
}
