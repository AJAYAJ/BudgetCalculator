package com.vegam.budgetcalculator.domain.model

data class Category(
    val id: String,
    val userId: String,
    val name: String,
    val icon: String,
    val color: Int, // stored as integer color value
    val isDefault: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
