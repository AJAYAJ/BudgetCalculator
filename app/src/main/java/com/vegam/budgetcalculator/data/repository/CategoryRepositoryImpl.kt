package com.vegam.budgetcalculator.data.repository

import com.vegam.budgetcalculator.data.local.dao.CategoryDao
import com.vegam.budgetcalculator.data.mapper.toDomain
import com.vegam.budgetcalculator.data.mapper.toEntity
import com.vegam.budgetcalculator.domain.model.Category
import com.vegam.budgetcalculator.domain.model.SubCategory
import com.vegam.budgetcalculator.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun observeCategories(userId: String): Flow<List<Category>> {
        return categoryDao.observeCategories(userId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addCategory(category: Category) {
        categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun addCategories(categories: List<Category>) {
        categoryDao.insertCategories(categories.map { it.toEntity() })
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category.toEntity())
    }

    override fun observeSubCategories(categoryId: String): Flow<List<SubCategory>> {
        return categoryDao.observeSubCategories(categoryId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addSubCategory(subCategory: SubCategory) {
        categoryDao.insertSubCategory(subCategory.toEntity())
    }

    override suspend fun addSubCategories(subCategories: List<SubCategory>) {
        categoryDao.insertSubCategories(subCategories.map { it.toEntity() })
    }
}
