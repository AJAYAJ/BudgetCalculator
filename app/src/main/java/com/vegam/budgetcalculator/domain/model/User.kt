package com.vegam.budgetcalculator.domain.model

data class User(
    val id: String,
    val firebaseUid: String,
    val email: String,
    val displayName: String,
    val createdAt: Long
)
