package com.vegam.budgetcalculator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val categoryId: String,
    val subCategoryId: String?,
    val amount: Long, // minor units (paise)
    val notes: String?,
    val expenseDate: Long, // timestamp for filtering/sorting
    val expenseTime: String, // string representation for UI if needed
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: Int
)
