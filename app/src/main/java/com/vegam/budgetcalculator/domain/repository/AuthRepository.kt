package com.vegam.budgetcalculator.domain.repository

import com.vegam.budgetcalculator.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserFlow: Flow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String): Result<User>
    suspend fun signUpWithFirebase(name: String, email: String, password: String): Result<User>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun logout()
    fun getCurrentUserId(): String?
}
