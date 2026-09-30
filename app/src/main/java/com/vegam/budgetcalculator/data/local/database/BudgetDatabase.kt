package com.vegam.budgetcalculator.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vegam.budgetcalculator.data.local.dao.CategoryDao
import com.vegam.budgetcalculator.data.local.dao.ExpenseDao
import com.vegam.budgetcalculator.data.local.dao.MonthlyBudgetDao
import com.vegam.budgetcalculator.data.local.dao.LoanDao
import com.vegam.budgetcalculator.data.local.entity.CategoryEntity
import com.vegam.budgetcalculator.data.local.entity.ExpenseEntity
import com.vegam.budgetcalculator.data.local.entity.MonthlyBudgetEntity
import com.vegam.budgetcalculator.data.local.entity.SubCategoryEntity
import com.vegam.budgetcalculator.data.local.entity.UserEntity
import com.vegam.budgetcalculator.data.local.entity.LoanPersonEntity
import com.vegam.budgetcalculator.data.local.entity.LoanTransactionEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        SubCategoryEntity::class,
        ExpenseEntity::class,
        MonthlyBudgetEntity::class,
        LoanPersonEntity::class,
        LoanTransactionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun monthlyBudgetDao(): MonthlyBudgetDao
    abstract fun loanDao(): LoanDao
}
