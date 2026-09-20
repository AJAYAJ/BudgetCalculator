package com.vegam.budgetcalculator.data.repository

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.vegam.budgetcalculator.data.local.dao.ExpenseDao
import com.vegam.budgetcalculator.data.mapper.toDomain
import com.vegam.budgetcalculator.data.mapper.toEntity
import com.vegam.budgetcalculator.data.remote.firebase.SyncWorker
import com.vegam.budgetcalculator.domain.model.Expense
import com.vegam.budgetcalculator.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val workManager: WorkManager
) : ExpenseRepository {

    override fun observeAllExpenses(userId: String): Flow<List<Expense>> {
        return expenseDao.observeAllExpenses(userId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeExpensesByDateRange(userId: String, startTime: Long, endTime: Long): Flow<List<Expense>> {
        return expenseDao.observeExpensesByDateRange(userId, startTime, endTime).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getExpenseById(id: String): Expense? {
        return expenseDao.getExpenseById(id)?.toDomain()
    }

    override suspend fun addExpense(expense: Expense) {
        expenseDao.insertExpense(expense.toEntity())
        scheduleSync()
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense.toEntity())
        scheduleSync()
    }

    override suspend fun deleteExpense(id: String) {
        expenseDao.deleteExpenseById(id)
        // Optionally sync deletion
    }

    override suspend fun getPendingSyncExpenses(userId: String): List<Expense> {
        return expenseDao.getPendingSyncExpenses(userId).map { it.toDomain() }
    }

    override suspend fun markSynced(id: String) {
        expenseDao.markSynced(id)
    }

    private fun scheduleSync() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        workManager.enqueue(syncRequest)
    }
}
