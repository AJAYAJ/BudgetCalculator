package com.vegam.budgetcalculator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vegam.budgetcalculator.data.local.entity.*

@Dao
interface BackupDao {
    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun categories(userId: String): List<CategoryEntity>

    @Query("SELECT * FROM subcategories WHERE categoryId IN (SELECT id FROM categories WHERE userId = :userId)")
    suspend fun subCategories(userId: String): List<SubCategoryEntity>

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    suspend fun expenses(userId: String): List<ExpenseEntity>

    @Query("SELECT * FROM monthly_budgets WHERE userId = :userId")
    suspend fun budgets(userId: String): List<MonthlyBudgetEntity>

    @Query("SELECT * FROM loan_people WHERE userId = :userId")
    suspend fun loanPeople(userId: String): List<LoanPersonEntity>

    @Query("SELECT * FROM loan_transactions WHERE personId IN (SELECT id FROM loan_people WHERE userId = :userId)")
    suspend fun loanTransactions(userId: String): List<LoanTransactionEntity>

    @Query("DELETE FROM expenses WHERE userId = :userId")
    suspend fun clearExpenses(userId: String)

    @Query("DELETE FROM monthly_budgets WHERE userId = :userId")
    suspend fun clearBudgets(userId: String)

    @Query("DELETE FROM subcategories WHERE categoryId IN (SELECT id FROM categories WHERE userId = :userId)")
    suspend fun clearSubCategories(userId: String)

    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun clearCategories(userId: String)

    @Query("DELETE FROM loan_transactions WHERE personId IN (SELECT id FROM loan_people WHERE userId = :userId)")
    suspend fun clearLoanTransactions(userId: String)

    @Query("DELETE FROM loan_people WHERE userId = :userId")
    suspend fun clearLoanPeople(userId: String)

    @Insert
    suspend fun insertCategories(values: List<CategoryEntity>)

    @Insert
    suspend fun insertSubCategories(values: List<SubCategoryEntity>)

    @Insert
    suspend fun insertExpenses(values: List<ExpenseEntity>)

    @Insert
    suspend fun insertBudgets(values: List<MonthlyBudgetEntity>)

    @Insert
    suspend fun insertLoanPeople(values: List<LoanPersonEntity>)

    @Insert
    suspend fun insertLoanTransactions(values: List<LoanTransactionEntity>)
}
