package com.vegam.budgetcalculator.data.repository

import com.vegam.budgetcalculator.data.local.dao.LoanDao
import com.vegam.budgetcalculator.data.local.entity.LoanPersonEntity
import com.vegam.budgetcalculator.data.local.entity.LoanTransactionEntity
import com.vegam.budgetcalculator.domain.model.LoanPerson
import com.vegam.budgetcalculator.domain.model.LoanSource
import com.vegam.budgetcalculator.domain.model.LoanTransaction
import com.vegam.budgetcalculator.domain.model.LoanTransactionType
import com.vegam.budgetcalculator.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepositoryImpl @Inject constructor(private val dao: LoanDao) : LoanRepository {
    override fun observePeople(userId: String): Flow<List<LoanPerson>> =
        dao.observePeople(userId).map { people -> people.map { it.toDomain() } }

    override fun observePerson(personId: String): Flow<LoanPerson?> =
        dao.observePerson(personId).map { it?.toDomain() }

    override fun observeTransactions(personId: String): Flow<List<LoanTransaction>> =
        dao.observeTransactions(personId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun addPerson(person: LoanPerson) = dao.insertPerson(person.toEntity())
    override suspend fun addTransaction(transaction: LoanTransaction) = dao.insertTransaction(transaction.toEntity())
    override suspend fun deleteTransaction(transactionId: String) = dao.deleteTransaction(transactionId)
}

private fun LoanPersonEntity.toDomain() = LoanPerson(id, userId, name, createdAt)
private fun LoanPerson.toEntity() = LoanPersonEntity(id, userId, name, createdAt)
private fun LoanTransactionEntity.toDomain() = LoanTransaction(
    id, personId, amountMinor, LoanTransactionType.valueOf(type), LoanSource.valueOf(source), transactionDate, createdAt
)
private fun LoanTransaction.toEntity() = LoanTransactionEntity(
    id, personId, amountMinor, type.name, source.name, transactionDate, createdAt
)
