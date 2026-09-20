package com.vegam.budgetcalculator.domain.model

data class MonthlyBudget(
    val id: String,
    val userId: String,
    val categoryId: String,
    val year: Int,
    val month: Int,
    val budgetAmountMinor: Long,
    val createdAt: Long,
    val updatedAt: Long
)
