package com.vegam.budgetcalculator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subcategories")
data class SubCategoryEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    val icon: String,
    val createdAt: Long,
    val updatedAt: Long
)
