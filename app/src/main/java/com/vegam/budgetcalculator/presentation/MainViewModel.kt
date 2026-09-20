package com.vegam.budgetcalculator.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vegam.budgetcalculator.domain.model.User
import com.vegam.budgetcalculator.domain.repository.AuthRepository
import com.vegam.budgetcalculator.domain.usecase.category.CreateDefaultCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val createDefaultCategories: CreateDefaultCategoriesUseCase
) : ViewModel() {

    init {
        viewModelScope.launch {
            authRepository.currentUserFlow.filterNotNull().collect { user ->
                createDefaultCategories(user.id)
            }
        }
    }

    val currentUser: StateFlow<User?> = authRepository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
