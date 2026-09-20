package com.vegam.budgetcalculator.di

import android.content.Context
import androidx.room.Room
import com.vegam.budgetcalculator.data.local.dao.CategoryDao
import com.vegam.budgetcalculator.data.local.dao.ExpenseDao
import com.vegam.budgetcalculator.data.local.dao.MonthlyBudgetDao
import com.vegam.budgetcalculator.data.local.database.BudgetDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBudgetDatabase(@ApplicationContext context: Context): BudgetDatabase {
        return Room.databaseBuilder(
            context,
            BudgetDatabase::class.java,
            "budget_calculator_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(db: BudgetDatabase): CategoryDao {
        return db.categoryDao()
    }

    @Provides
    @Singleton
    fun provideExpenseDao(db: BudgetDatabase): ExpenseDao {
        return db.expenseDao()
    }

    @Provides
    @Singleton
    fun provideMonthlyBudgetDao(db: BudgetDatabase): MonthlyBudgetDao {
        return db.monthlyBudgetDao()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): androidx.work.WorkManager {
        return androidx.work.WorkManager.getInstance(context)
    }
}
