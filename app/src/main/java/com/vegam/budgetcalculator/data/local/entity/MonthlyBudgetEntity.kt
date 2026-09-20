package com.vegam.budgetcalculator.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_budgets",
    indices = [Index(value = ["userId", "categoryId", "year", "month"], unique = true)]
)
data class MonthlyBudgetEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val categoryId: String,
    val year: Int,
    val month: Int,
    val budgetAmount: Long, // minor units
    val createdAt: Long,
    val updatedAt: Long
)
