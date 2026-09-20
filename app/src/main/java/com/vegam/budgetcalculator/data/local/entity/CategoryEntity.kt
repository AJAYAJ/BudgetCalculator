package com.vegam.budgetcalculator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val icon: String,
    val color: Int,
    val isDefault: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
