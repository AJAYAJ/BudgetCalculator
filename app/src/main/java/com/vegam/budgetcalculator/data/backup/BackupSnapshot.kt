package com.vegam.budgetcalculator.data.backup

import com.vegam.budgetcalculator.data.local.entity.*
import kotlinx.serialization.Serializable

@Serializable
data class BackupSnapshot(
    val format: String = "BudgetCalculator",
    val version: Int = 1,
    val userId: String,
    val createdAt: Long,
    val categories: List<CategoryEntity>,
    val subCategories: List<SubCategoryEntity>,
    val expenses: List<ExpenseEntity>,
    val budgets: List<MonthlyBudgetEntity>,
    val loanPeople: List<LoanPersonEntity>,
    val loanTransactions: List<LoanTransactionEntity>
) {
    fun validate(expectedUserId: String) {
        require(format == "BudgetCalculator" && version == 1) { "Unsupported backup format or version." }
        require(userId == expectedUserId) { "Sign in to the app account that created this backup." }
        require(createdAt > 0) { "Invalid backup date." }
        require(categories.all { it.userId == userId } && expenses.all { it.userId == userId } &&
            budgets.all { it.userId == userId } && loanPeople.all { it.userId == userId }) {
            "Backup contains records from another account."
        }
        listOf(categories.map { it.id }, subCategories.map { it.id }, expenses.map { it.id },
            budgets.map { it.id }, loanPeople.map { it.id }, loanTransactions.map { it.id }).forEach { ids ->
            require(ids.all { it.isNotBlank() } && ids.toSet().size == ids.size) { "Invalid or duplicate record IDs." }
        }
        val categoryIds = categories.map { it.id }.toSet()
        val subCategoriesById = subCategories.associateBy { it.id }
        val personIds = loanPeople.map { it.id }.toSet()
        require(subCategories.all { it.categoryId in categoryIds }) { "Backup has missing categories." }
        require(expenses.all { expense ->
            expense.categoryId in categoryIds && (expense.subCategoryId == null ||
                subCategoriesById[expense.subCategoryId]?.categoryId == expense.categoryId)
        }) { "Backup has invalid expense categories." }
        require(loanTransactions.all { it.personId in personIds && it.amountMinor > 0 &&
            it.type in setOf("GIVEN", "TAKEN", "INTEREST") &&
            it.source in setOf("GOOGLE_PAY", "PHONE_PAY", "CASH", "OTHER") }) { "Invalid loan records." }
        require(budgets.all { it.month in 1..12 } &&
            budgets.map { Triple(it.categoryId, it.year, it.month) }.toSet().size == budgets.size) {
            "Invalid or duplicate monthly budgets."
        }
    }
}
