package com.vegam.budgetcalculator

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vegam.budgetcalculator.data.backup.BackupRepository
import com.vegam.budgetcalculator.data.backup.BackupSnapshot
import com.vegam.budgetcalculator.data.backup.DriveBackupService
import com.vegam.budgetcalculator.data.local.database.BudgetDatabase
import com.vegam.budgetcalculator.data.local.entity.CategoryEntity
import com.vegam.budgetcalculator.domain.model.User
import com.vegam.budgetcalculator.domain.repository.AuthRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupRestoreTest {
    private lateinit var database: BudgetDatabase
    private lateinit var repository: BackupRepository
    private val auth = object : AuthRepository {
        override val currentUserFlow = flowOf<User?>(null)
        override fun getCurrentUserId() = "user-1"
        override suspend fun login(email: String, password: String): Result<User> = error("Unused")
        override suspend fun register(name: String, email: String): Result<User> = error("Unused")
        override suspend fun signUpWithFirebase(name: String, email: String, password: String): Result<User> = error("Unused")
        override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = error("Unused")
        override suspend fun logout() = Unit
    }

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), BudgetDatabase::class.java).build()
        repository = BackupRepository(database, auth, DriveBackupService())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun category(id: String, userId: String) = CategoryEntity(id, userId, "Food", "", 0, false, 1, 1)

    private fun snapshot(category: CategoryEntity) = BackupSnapshot(
        userId = "user-1", createdAt = 1, categories = listOf(category),
        subCategories = emptyList(), expenses = emptyList(), budgets = emptyList(),
        loanPeople = emptyList(), loanTransactions = emptyList()
    )

    @Test
    fun restoreReplacesOnlyCurrentAccountAndIsRepeatable() = runBlocking {
        val dao = database.backupDao()
        val other = category("other", "user-2")
        dao.insertCategories(listOf(category("old", "user-1"), other))
        val restored = category("restored", "user-1")
        repository.restore(snapshot(restored))
        repository.restore(snapshot(restored))
        assertEquals(listOf(restored), dao.categories("user-1"))
        assertEquals(listOf(other), dao.categories("user-2"))
    }

    @Test
    fun conflictingOtherAccountIdRollsBackAllDeletes() = runBlocking {
        val dao = database.backupDao()
        val original = category("original", "user-1")
        val other = category("conflict", "user-2")
        dao.insertCategories(listOf(original, other))
        var failed = false
        try {
            repository.restore(snapshot(category("conflict", "user-1")))
        } catch (_: android.database.sqlite.SQLiteConstraintException) {
            failed = true
        }
        assertTrue(failed)
        assertEquals(listOf(original), dao.categories("user-1"))
        assertEquals(listOf(other), dao.categories("user-2"))
    }
}
