package com.vegam.budgetcalculator.data.backup

import androidx.room.withTransaction
import com.vegam.budgetcalculator.data.local.database.BudgetDatabase
import com.vegam.budgetcalculator.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val database: BudgetDatabase,
    private val authRepository: AuthRepository,
    private val drive: DriveBackupService
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun latest(token: String, userId: String): DriveBackupFile? = drive.latest(token, userId)

    suspend fun backup(token: String, userId: String) = withContext(Dispatchers.IO) {
        checkUser(userId)
        val dao = database.backupDao()
        val snapshot = database.withTransaction {
            BackupSnapshot(
                userId = userId,
                createdAt = System.currentTimeMillis(),
                categories = dao.categories(userId),
                subCategories = dao.subCategories(userId),
                expenses = dao.expenses(userId),
                budgets = dao.budgets(userId),
                loanPeople = dao.loanPeople(userId),
                loanTransactions = dao.loanTransactions(userId)
            )
        }
        snapshot.validate(userId)
        checkUser(userId)
        drive.upload(token, userId, json.encodeToString(snapshot))
    }

    suspend fun prepareRestore(token: String, userId: String): BackupSnapshot = withContext(Dispatchers.IO) {
        checkUser(userId)
        val file = drive.latest(token, userId) ?: error("No backup found. Use the same Google and app accounts as before.")
        val snapshot = json.decodeFromString<BackupSnapshot>(drive.download(token, file))
        snapshot.validate(userId)
        checkUser(userId)
        snapshot
    }

    suspend fun restore(snapshot: BackupSnapshot) = withContext(Dispatchers.IO) {
        checkUser(snapshot.userId)
        snapshot.validate(snapshot.userId)
        val dao = database.backupDao()
        database.withTransaction {
            checkUser(snapshot.userId)
            dao.clearExpenses(snapshot.userId)
            dao.clearBudgets(snapshot.userId)
            dao.clearSubCategories(snapshot.userId)
            dao.clearCategories(snapshot.userId)
            dao.clearLoanTransactions(snapshot.userId)
            dao.clearLoanPeople(snapshot.userId)
            dao.insertCategories(snapshot.categories)
            dao.insertSubCategories(snapshot.subCategories)
            dao.insertExpenses(snapshot.expenses)
            dao.insertBudgets(snapshot.budgets)
            dao.insertLoanPeople(snapshot.loanPeople)
            dao.insertLoanTransactions(snapshot.loanTransactions)
        }
    }

    private fun checkUser(userId: String) {
        check(authRepository.getCurrentUserId() == userId) { "Your app account changed. Sign in again before continuing." }
    }
}
