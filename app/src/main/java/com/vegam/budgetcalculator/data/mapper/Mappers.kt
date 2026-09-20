package com.vegam.budgetcalculator.data.mapper

import com.vegam.budgetcalculator.data.local.entity.CategoryEntity
import com.vegam.budgetcalculator.data.local.entity.ExpenseEntity
import com.vegam.budgetcalculator.data.local.entity.MonthlyBudgetEntity
import com.vegam.budgetcalculator.data.local.entity.SubCategoryEntity
import com.vegam.budgetcalculator.domain.model.Category
import com.vegam.budgetcalculator.domain.model.Expense
import com.vegam.budgetcalculator.domain.model.MonthlyBudget
import com.vegam.budgetcalculator.domain.model.SubCategory

fun CategoryEntity.toDomain() = Category(
    id = id,
    userId = userId,
    name = name,
    icon = icon,
    color = color,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    userId = userId,
    name = name,
    icon = icon,
    color = color,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SubCategoryEntity.toDomain() = SubCategory(
    id = id,
    categoryId = categoryId,
    name = name,
    icon = icon,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SubCategory.toEntity() = SubCategoryEntity(
    id = id,
    categoryId = categoryId,
    name = name,
    icon = icon,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ExpenseEntity.toDomain() = Expense(
    id = id,
    userId = userId,
    categoryId = categoryId,
    subCategoryId = subCategoryId,
    amountMinor = amount,
    notes = notes,
    dateTime = expenseDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)

fun Expense.toEntity() = ExpenseEntity(
    id = id,
    userId = userId,
    categoryId = categoryId,
    subCategoryId = subCategoryId,
    amount = amountMinor,
    notes = notes,
    expenseDate = dateTime,
    expenseTime = "",
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)

fun MonthlyBudgetEntity.toDomain() = MonthlyBudget(
    id = id,
    userId = userId,
    categoryId = categoryId,
    year = year,
    month = month,
    budgetAmountMinor = budgetAmount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun MonthlyBudget.toEntity() = MonthlyBudgetEntity(
    id = id,
    userId = userId,
    categoryId = categoryId,
    year = year,
    month = month,
    budgetAmount = budgetAmountMinor,
    createdAt = createdAt,
    updatedAt = updatedAt
)
