package com.vegam.budgetcalculator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vegam.budgetcalculator.data.local.entity.LoanPersonEntity
import com.vegam.budgetcalculator.data.local.entity.LoanTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loan_people WHERE userId = :userId ORDER BY name COLLATE NOCASE")
    fun observePeople(userId: String): Flow<List<LoanPersonEntity>>

    @Query("SELECT * FROM loan_people WHERE id = :personId")
    fun observePerson(personId: String): Flow<LoanPersonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: LoanPersonEntity)

    @Query("SELECT * FROM loan_transactions WHERE personId = :personId ORDER BY transactionDate DESC, createdAt DESC")
    fun observeTransactions(personId: String): Flow<List<LoanTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: LoanTransactionEntity)

    @Query("DELETE FROM loan_transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: String)
}
