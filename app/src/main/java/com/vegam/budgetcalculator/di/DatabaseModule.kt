package com.vegam.budgetcalculator.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vegam.budgetcalculator.data.local.dao.CategoryDao
import com.vegam.budgetcalculator.data.local.dao.ExpenseDao
import com.vegam.budgetcalculator.data.local.dao.MonthlyBudgetDao
import com.vegam.budgetcalculator.data.local.dao.LoanDao
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

    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `loan_people` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_loan_people_userId_name` ON `loan_people` (`userId`, `name`)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `loan_transactions` (`id` TEXT NOT NULL, `personId` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `type` TEXT NOT NULL, `source` TEXT NOT NULL, `transactionDate` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`personId`) REFERENCES `loan_people`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_loan_transactions_personId` ON `loan_transactions` (`personId`)")
        }
    }

    @Provides
    @Singleton
    fun provideBudgetDatabase(@ApplicationContext context: Context): BudgetDatabase {
        return Room.databaseBuilder(
            context,
            BudgetDatabase::class.java,
            "budget_calculator_db"
        ).addMigrations(migration1To2).build()
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
    fun provideLoanDao(db: BudgetDatabase): LoanDao = db.loanDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): androidx.work.WorkManager {
        return androidx.work.WorkManager.getInstance(context)
    }
}
