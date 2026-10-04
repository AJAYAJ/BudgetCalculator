package com.vegam.budgetcalculator.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@kotlinx.serialization.Serializable
@Entity(
    tableName = "loan_people",
    indices = [Index(value = ["userId", "name"])]
)
data class LoanPersonEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val createdAt: Long
)
