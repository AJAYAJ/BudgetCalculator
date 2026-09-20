package com.vegam.budgetcalculator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val firebaseUid: String,
    val email: String,
    val displayName: String,
    val createdAt: Long
)
