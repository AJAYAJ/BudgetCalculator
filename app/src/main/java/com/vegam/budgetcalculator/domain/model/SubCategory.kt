package com.vegam.budgetcalculator.domain.model

data class SubCategory(
    val id: String,
    val categoryId: String,
    val name: String,
    val icon: String,
    val createdAt: Long,
    val updatedAt: Long
)
