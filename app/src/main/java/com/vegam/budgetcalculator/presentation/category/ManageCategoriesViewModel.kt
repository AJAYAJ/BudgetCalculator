package com.vegam.budgetcalculator.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vegam.budgetcalculator.domain.model.Category
import com.vegam.budgetcalculator.domain.model.SubCategory
import com.vegam.budgetcalculator.domain.repository.AuthRepository
import com.vegam.budgetcalculator.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ManageCategoriesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _deleteRequest = MutableStateFlow<CategoryDeleteRequest?>(null)
    val deleteRequest = _deleteRequest.asStateFlow()

    val categories: StateFlow<List<Category>> = authRepository.currentUserFlow
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else categoryRepository.observeCategories(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val subCategories: StateFlow<Map<String, List<SubCategory>>> = categories
        .flatMapLatest { currentCategories ->
            if (currentCategories.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(currentCategories.map { category ->
                    categoryRepository.observeSubCategories(category.id)
                }) { lists ->
                    currentCategories.mapIndexed { index, category ->
                        category.id to lists[index]
                    }.toMap()
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun saveCategory(existing: Category?, name: String, icon: String, color: Int) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (existing == null) {
                val userId = authRepository.getCurrentUserId() ?: return@launch
                categoryRepository.addCategory(
                    Category(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        name = trimmedName,
                        icon = icon,
                        color = color,
                        isDefault = false,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            } else {
                categoryRepository.updateCategory(
                    existing.copy(name = trimmedName, icon = icon, color = color, updatedAt = now)
                )
            }
        }
    }

    fun saveSubCategory(categoryId: String, existing: SubCategory?, name: String, icon: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (existing == null) {
                categoryRepository.addSubCategory(
                    SubCategory(
                        id = UUID.randomUUID().toString(),
                        categoryId = categoryId,
                        name = trimmedName,
                        icon = icon,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            } else {
                categoryRepository.updateSubCategory(
                    existing.copy(name = trimmedName, icon = icon, updatedAt = now)
                )
            }
        }
    }

    fun requestDeleteCategory(category: Category) {
        viewModelScope.launch {
            val expenseCount = categoryRepository.getExpenseCountForCategory(category.id)
            if (expenseCount == 0) {
                categoryRepository.deleteCategoryAndExpenses(category)
            } else {
                _deleteRequest.value = CategoryDeleteRequest.CategoryRequest(category, expenseCount)
            }
        }
    }

    fun requestDeleteSubCategory(subCategory: SubCategory) {
        viewModelScope.launch {
            val expenseCount = categoryRepository.getExpenseCountForSubCategory(subCategory.id)
            if (expenseCount == 0) {
                categoryRepository.deleteSubCategoryAndExpenses(subCategory)
            } else {
                _deleteRequest.value = CategoryDeleteRequest.SubCategoryRequest(subCategory, expenseCount)
            }
        }
    }

    fun confirmDelete() {
        val request = _deleteRequest.value ?: return
        viewModelScope.launch {
            when (request) {
                is CategoryDeleteRequest.CategoryRequest -> categoryRepository.deleteCategoryAndExpenses(request.category)
                is CategoryDeleteRequest.SubCategoryRequest -> categoryRepository.deleteSubCategoryAndExpenses(request.subCategory)
            }
            _deleteRequest.value = null
        }
    }

    fun dismissDeleteRequest() {
        _deleteRequest.value = null
    }
}

sealed interface CategoryDeleteRequest {
    val expenseCount: Int

    data class CategoryRequest(val category: Category, override val expenseCount: Int) : CategoryDeleteRequest
    data class SubCategoryRequest(val subCategory: SubCategory, override val expenseCount: Int) : CategoryDeleteRequest
}
