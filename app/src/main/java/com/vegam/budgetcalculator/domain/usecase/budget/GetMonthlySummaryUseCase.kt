package com.vegam.budgetcalculator.domain.usecase.budget

import com.vegam.budgetcalculator.domain.model.Expense
import com.vegam.budgetcalculator.domain.model.MonthlyBudget
import javax.inject.Inject

data class MonthlySummary(
    val totalBudget: Long,
    val totalSpent: Long,
    val remaining: Long,
    val percentage: Float
)

class GetMonthlySummaryUseCase @Inject constructor() {
    operator fun invoke(budgets: List<MonthlyBudget>, expenses: List<Expense>): MonthlySummary {
        val totalBudget = budgets.sumOf { it.budgetAmountMinor }
        val totalSpent = expenses.sumOf { it.amountMinor }
        val remaining = totalBudget - totalSpent
        val percentage = if (totalBudget > 0) (totalSpent.toFloat() / totalBudget.toFloat()) * 100f else 0f
        
        return MonthlySummary(
            totalBudget = totalBudget,
            totalSpent = totalSpent,
            remaining = remaining,
            percentage = percentage
        )
    }
}
