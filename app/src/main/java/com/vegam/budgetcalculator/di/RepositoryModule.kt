package com.vegam.budgetcalculator.di

import com.vegam.budgetcalculator.data.repository.AuthRepositoryImpl
import com.vegam.budgetcalculator.data.repository.BudgetRepositoryImpl
import com.vegam.budgetcalculator.data.repository.CategoryRepositoryImpl
import com.vegam.budgetcalculator.data.repository.ExpenseRepositoryImpl
import com.vegam.budgetcalculator.domain.repository.AuthRepository
import com.vegam.budgetcalculator.domain.repository.BudgetRepository
import com.vegam.budgetcalculator.domain.repository.CategoryRepository
import com.vegam.budgetcalculator.domain.repository.ExpenseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        impl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        impl: BudgetRepositoryImpl
    ): BudgetRepository
}
