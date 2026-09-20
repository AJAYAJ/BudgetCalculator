package com.vegam.budgetcalculator.domain.usecase.analytics

import com.vegam.budgetcalculator.domain.model.Category
import com.vegam.budgetcalculator.domain.model.Expense
import com.vegam.budgetcalculator.domain.model.MonthlyBudget
import javax.inject.Inject

data class CategorySpending(
    val category: Category,
    val budgetAmount: Long,
    val spentAmount: Long,
    val remainingAmount: Long,
    val percentage: Float
)

class GetCategorySpendingUseCase @Inject constructor() {
    operator fun invoke(
        categories: List<Category>,
        budgets: List<MonthlyBudget>,
        expenses: List<Expense>
    ): List<CategorySpending> {
        return categories.map { category ->
            val categoryBudget = budgets.find { it.categoryId == category.id }?.budgetAmountMinor ?: 0L
            val categorySpent = expenses.filter { it.categoryId == category.id }.sumOf { it.amountMinor }
            val remaining = categoryBudget - categorySpent
            val percentage = if (categoryBudget > 0) (categorySpent.toFloat() / categoryBudget.toFloat()) * 100f else 0f
            
            CategorySpending(
                category = category,
                budgetAmount = categoryBudget,
                spentAmount = categorySpent,
                remainingAmount = remaining,
                percentage = percentage
            )
        }.sortedByDescending { it.spentAmount }
    }
}
