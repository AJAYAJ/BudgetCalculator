package com.vegam.budgetcalculator.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@kotlinx.serialization.Serializable
@Entity(
    tableName = "loan_transactions",
    foreignKeys = [
        ForeignKey(
            entity = LoanPersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId")]
)
data class LoanTransactionEntity(
    @PrimaryKey val id: String,
    val personId: String,
    val amountMinor: Long,
    val type: String,
    val source: String,
    val transactionDate: Long,
    val createdAt: Long
)
