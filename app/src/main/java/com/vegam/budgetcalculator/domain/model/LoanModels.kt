package com.vegam.budgetcalculator.domain.model

data class LoanPerson(
    val id: String,
    val userId: String,
    val name: String,
    val createdAt: Long
)

enum class LoanTransactionType { GIVEN, TAKEN, INTEREST }

enum class LoanSource { GOOGLE_PAY, PHONE_PAY, CASH, OTHER }

data class LoanTransaction(
    val id: String,
    val personId: String,
    val amountMinor: Long,
    val type: LoanTransactionType,
    val source: LoanSource,
    val transactionDate: Long,
    val createdAt: Long
) {
    val signedAmountMinor: Long
        get() = if (type == LoanTransactionType.GIVEN) -amountMinor else amountMinor
}
