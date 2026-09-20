package com.vegam.budgetcalculator.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.vegam.budgetcalculator.domain.model.Expense
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun uploadExpense(userId: String, expense: Expense) {
        firestore.collection("users")
            .document(userId)
            .collection("expenses")
            .document(expense.id)
            .set(expense)
            .await()
    }

    suspend fun getExpenses(userId: String): List<Expense> {
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("expenses")
            .get()
            .await()
        return snapshot.toObjects(Expense::class.java)
    }
}
