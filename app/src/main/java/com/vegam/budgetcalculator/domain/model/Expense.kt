package com.vegam.budgetcalculator.domain.model

data class Expense(
    val id: String,
    val userId: String,
    val categoryId: String,
    val subCategoryId: String?,
    val amountMinor: Long,
    val notes: String?,
    val dateTime: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: Int = 0
)
