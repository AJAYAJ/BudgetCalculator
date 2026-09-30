package com.vegam.budgetcalculator.presentation.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vegam.budgetcalculator.domain.model.LoanPerson
import com.vegam.budgetcalculator.domain.repository.AuthRepository
import com.vegam.budgetcalculator.domain.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LoansViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val repository: LoanRepository
) : ViewModel() {
    val people: StateFlow<List<LoanPerson>> = authRepository.currentUserFlow
        .flatMapLatest { user -> if (user == null) flowOf(emptyList()) else repository.observePeople(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addPerson(name: String) {
        val cleanName = name.trim()
        val userId = authRepository.getCurrentUserId() ?: return
        if (cleanName.isEmpty()) return
        viewModelScope.launch {
            repository.addPerson(LoanPerson(UUID.randomUUID().toString(), userId, cleanName, System.currentTimeMillis()))
        }
    }
}
