package com.vegam.budgetcalculator.domain.repository

import com.vegam.budgetcalculator.domain.model.LoanPerson
import com.vegam.budgetcalculator.domain.model.LoanTransaction
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    fun observePeople(userId: String): Flow<List<LoanPerson>>
    fun observePerson(personId: String): Flow<LoanPerson?>
    fun observeTransactions(personId: String): Flow<List<LoanTransaction>>
    suspend fun addPerson(person: LoanPerson)
    suspend fun addTransaction(transaction: LoanTransaction)
    suspend fun deleteTransaction(transactionId: String)
}
