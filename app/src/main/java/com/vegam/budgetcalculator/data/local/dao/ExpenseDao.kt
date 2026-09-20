package com.vegam.budgetcalculator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vegam.budgetcalculator.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY expenseDate DESC, id DESC")
    fun observeAllExpenses(userId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND expenseDate >= :startRow AND expenseDate <= :endRow ORDER BY expenseDate DESC")
    fun observeExpensesByDateRange(userId: String, startRow: Long, endRow: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: String): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: String)

    @Query("SELECT * FROM expenses WHERE userId = :userId AND syncStatus = 0")
    suspend fun getPendingSyncExpenses(userId: String): List<ExpenseEntity>

    @Query("UPDATE expenses SET syncStatus = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}
