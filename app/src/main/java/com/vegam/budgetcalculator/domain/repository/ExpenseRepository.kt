package com.vegam.budgetcalculator.domain.repository

import com.vegam.budgetcalculator.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeAllExpenses(userId: String): Flow<List<Expense>>
    fun observeExpensesByDateRange(userId: String, startTime: Long, endTime: Long): Flow<List<Expense>>
    suspend fun getExpenseById(id: String): Expense?
    suspend fun addExpense(expense: Expense)
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(id: String)
    suspend fun getPendingSyncExpenses(userId: String): List<Expense>
    suspend fun markSynced(id: String)
}
