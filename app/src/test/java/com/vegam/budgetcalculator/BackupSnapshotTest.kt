package com.vegam.budgetcalculator

import com.vegam.budgetcalculator.data.backup.BackupSnapshot
import com.vegam.budgetcalculator.data.local.entity.*
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupSnapshotTest {
    private fun snapshot() = BackupSnapshot(
        userId = "user-1",
        createdAt = 1000,
        categories = listOf(CategoryEntity("food", "user-1", "Food", "🍽️", 1, false, 1, 1)),
        subCategories = listOf(SubCategoryEntity("lunch", "food", "Lunch", "🍽️", 1, 1)),
        expenses = listOf(ExpenseEntity("expense", "user-1", "food", "lunch", 12345,
            "Lunch", 1000, "12:00", 1, 1, 0)),
        budgets = listOf(MonthlyBudgetEntity("budget", "user-1", "food", 2026, 1, 50000, 1, 1)),
        loanPeople = listOf(LoanPersonEntity("person", "user-1", "Alex", 1)),
        loanTransactions = listOf(LoanTransactionEntity("loan", "person", 10000, "GIVEN", "CASH", 1, 1))
    )

    @Test
    fun roundTripPreservesAllRecordsAndMinorUnits() {
        val original = snapshot()
        val json = Json { encodeDefaults = true }
        val restored = json.decodeFromString<BackupSnapshot>(json.encodeToString(original))
        restored.validate("user-1")
        assertEquals(original, restored)
        assertEquals(12345L, restored.expenses.single().amount)
    }

    @Test
    fun rejectsDifferentAppAccount() {
        assertThrows(IllegalArgumentException::class.java) { snapshot().validate("user-2") }
    }

    @Test
    fun rejectsMixedAccountRecords() {
        val original = snapshot()
        val invalid = original.copy(expenses = original.expenses.map { it.copy(userId = "user-2") })
        assertThrows(IllegalArgumentException::class.java) { invalid.validate("user-1") }
    }

    @Test
    fun rejectsUnknownVersion() {
        assertThrows(IllegalArgumentException::class.java) { snapshot().copy(version = 2).validate("user-1") }
    }

    @Test
    fun rejectsDuplicateIds() {
        val original = snapshot()
        assertThrows(IllegalArgumentException::class.java) {
            original.copy(expenses = original.expenses + original.expenses).validate("user-1")
        }
    }

    @Test
    fun rejectsMissingCategoryAndWrongSubcategory() {
        assertThrows(IllegalArgumentException::class.java) { snapshot().copy(categories = emptyList()).validate("user-1") }
        val original = snapshot()
        assertThrows(IllegalArgumentException::class.java) {
            original.copy(expenses = original.expenses.map { it.copy(subCategoryId = "missing") }).validate("user-1")
        }
    }

    @Test
    fun rejectsMissingLoanPersonAndUnknownEnum() {
        assertThrows(IllegalArgumentException::class.java) { snapshot().copy(loanPeople = emptyList()).validate("user-1") }
        val original = snapshot()
        assertThrows(IllegalArgumentException::class.java) {
            original.copy(loanTransactions = original.loanTransactions.map { it.copy(type = "UNKNOWN") }).validate("user-1")
        }
    }

    @Test
    fun rejectsDuplicateBudgetPeriod() {
        val original = snapshot()
        assertThrows(IllegalArgumentException::class.java) {
            original.copy(budgets = original.budgets + original.budgets.single().copy(id = "another"))
                .validate("user-1")
        }
    }

    @Test
    fun acceptsAnEmptyAccount() {
        snapshot().copy(categories = emptyList(), subCategories = emptyList(), expenses = emptyList(),
            budgets = emptyList(), loanPeople = emptyList(), loanTransactions = emptyList()).validate("user-1")
    }
}
