package com.vegam.budgetcalculator.domain.repository

import com.vegam.budgetcalculator.domain.model.Category
import com.vegam.budgetcalculator.domain.model.SubCategory
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(userId: String): Flow<List<Category>>
    suspend fun hasCategories(userId: String): Boolean
    suspend fun addCategory(category: Category)
    suspend fun addCategories(categories: List<Category>)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
    
    // Subcategories
    fun observeSubCategories(categoryId: String): Flow<List<SubCategory>>
    suspend fun addSubCategory(subCategory: SubCategory)
    suspend fun addSubCategories(subCategories: List<SubCategory>)
    suspend fun updateSubCategory(subCategory: SubCategory)
    suspend fun deleteSubCategory(subCategory: SubCategory)
}
