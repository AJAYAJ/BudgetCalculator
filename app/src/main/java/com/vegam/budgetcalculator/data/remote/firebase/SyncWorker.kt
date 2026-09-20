package com.vegam.budgetcalculator.data.remote.firebase

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vegam.budgetcalculator.data.remote.firestore.FirestoreService
import com.vegam.budgetcalculator.domain.repository.AuthRepository
import com.vegam.budgetcalculator.domain.repository.ExpenseRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val authRepository: AuthRepository,
    private val expenseRepository: ExpenseRepository,
    private val firestoreService: FirestoreService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userId = authRepository.getCurrentUserId() ?: return Result.success()

        return try {
            val pendingExpenses = expenseRepository.getPendingSyncExpenses(userId)
            pendingExpenses.forEach { expense ->
                firestoreService.uploadExpense(userId, expense)
                expenseRepository.markSynced(expense.id)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
