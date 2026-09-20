package com.vegam.budgetcalculator.domain.usecase.expense

import com.vegam.budgetcalculator.domain.model.Expense
import com.vegam.budgetcalculator.domain.repository.ExpenseRepository
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense): Result<Unit> {
        if (expense.amountMinor <= 0) {
            return Result.failure(Exception("Amount must be greater than zero"))
        }
        repository.addExpense(expense)
        return Result.success(Unit)
    }
}
