package com.vegam.budgetcalculator.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.vegam.budgetcalculator.domain.model.User
import com.vegam.budgetcalculator.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUserFlow: Flow<User?> = callbackFlow {
        val initialUser = firebaseAuth.currentUser?.let { firebaseUser ->
            User(
                id = firebaseUser.uid,
                firebaseUid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "User",
                createdAt = System.currentTimeMillis()
            )
        }
        trySend(initialUser)

        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                trySend(
                    User(
                        id = firebaseUser.uid,
                        firebaseUid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "User",
                        createdAt = System.currentTimeMillis()
                    )
                )
            } else {
                trySend(null)
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("User null after successful sign in")
            Result.success(
                User(
                    id = firebaseUser.uid,
                    firebaseUid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "User",
                    createdAt = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(name: String, email: String): Result<User> {
        // Basic method, typically called inside signUpWithFirebase
        val firebaseUser = firebaseAuth.currentUser ?: return Result.failure(Exception("No current firebase user"))
        return Result.success(
            User(
                id = firebaseUser.uid,
                firebaseUid = firebaseUser.uid,
                email = firebaseUser.email ?: email,
                displayName = name,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun signUpWithFirebase(name: String, email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("User null after successful sign up")
            
            // Optionally update profile display name
            val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                displayName = name
            }
            firebaseUser.updateProfile(profileUpdates).await()

            Result.success(
                User(
                    id = firebaseUser.uid,
                    firebaseUid = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    displayName = name,
                    createdAt = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}
