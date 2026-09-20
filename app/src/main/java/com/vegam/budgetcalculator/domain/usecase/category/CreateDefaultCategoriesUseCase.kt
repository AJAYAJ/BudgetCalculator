package com.vegam.budgetcalculator.domain.usecase.category

import com.vegam.budgetcalculator.domain.model.Category
import com.vegam.budgetcalculator.domain.model.SubCategory
import com.vegam.budgetcalculator.domain.repository.CategoryRepository
import java.util.UUID
import javax.inject.Inject

class CreateDefaultCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(userId: String) {
        if (repository.hasCategories(userId)) return

        val defaults = listOf(
            "Home Needs" to listOf("Vegetables", "Groceries", "Cleaning", "Milk", "Household Products"),
            "Groceries" to emptyList(),
            "Vegetables" to emptyList(),
            "Petrol / Fuel" to emptyList(),
            "Water Bill" to emptyList(),
            "Electricity Bill" to emptyList(),
            "Personal" to emptyList(),
            "Hospital / Medical" to listOf("Medicine", "Doctor", "Hospital", "Tests"),
            "Credit Card Bill" to emptyList(),
            "Rent" to emptyList(),
            "Food" to emptyList(),
            "Travel" to listOf("Petrol", "Bus", "Train", "Flight", "Cab"),
            "Shopping" to emptyList(),
            "Education" to emptyList(),
            "Entertainment" to emptyList(),
            "EMI" to emptyList(),
            "Insurance" to emptyList(),
            "Other" to emptyList()
        )

        // default palette colors represented as Ints
        val colors = listOf(
            0xFFF44336.toInt(), 0xFFE91E63.toInt(), 0xFF9C27B0.toInt(), 0xFF673AB7.toInt(),
            0xFF3F51B5.toInt(), 0xFF2196F3.toInt(), 0xFF03A9F4.toInt(), 0xFF00BCD4.toInt(),
            0xFF009688.toInt(), 0xFF4CAF50.toInt(), 0xFF8BC34A.toInt(), 0xFFCDDC39.toInt(),
            0xFFFFEB3B.toInt(), 0xFFFFC107.toInt(), 0xFFFF9800.toInt(), 0xFFFF5722.toInt(),
            0xFF795548.toInt(), 0xFF9E9E9E.toInt()
        )

        val categoriesToInsert = mutableListOf<Category>()
        val subCategoriesToInsert = mutableListOf<SubCategory>()

        val icons = listOf(
            "🏠", "🛒", "🥬", "⛽", "💧", "⚡", "👤", "🏥", "💳",
            "🔑", "🍽️", "✈️", "🛍️", "🎓", "🎬", "🏦", "🛡️", "📦"
        )

        defaults.forEachIndexed { index, pair ->
            val categoryId = UUID.randomUUID().toString()
            val color = colors[index % colors.size]
            categoriesToInsert.add(
                Category(
                    id = categoryId,
                    userId = userId,
                    name = pair.first,
                    icon = icons[index],
                    color = color,
                    isDefault = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )

            pair.second.forEach { subName ->
                subCategoriesToInsert.add(
                    SubCategory(
                        id = UUID.randomUUID().toString(),
                        categoryId = categoryId,
                        name = subName,
                        icon = "•",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        repository.addCategories(categoriesToInsert)
        repository.addSubCategories(subCategoriesToInsert)
    }
}
