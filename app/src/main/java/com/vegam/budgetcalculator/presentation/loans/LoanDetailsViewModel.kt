package com.vegam.budgetcalculator.presentation.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vegam.budgetcalculator.domain.model.LoanPerson
import com.vegam.budgetcalculator.domain.model.LoanSource
import com.vegam.budgetcalculator.domain.model.LoanTransaction
import com.vegam.budgetcalculator.domain.model.LoanTransactionType
import com.vegam.budgetcalculator.domain.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class LoanDetailsViewModel @Inject constructor(
    private val repository: LoanRepository
) : ViewModel() {
    private val personId = MutableStateFlow<String?>(null)

    val person: StateFlow<LoanPerson?> = personId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.observePerson(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val transactions: StateFlow<List<LoanTransaction>> = personId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.observeTransactions(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load(id: String) {
        personId.value = id
    }

    fun addTransaction(amount: String, source: LoanSource, type: LoanTransactionType, date: Long) {
        val id = personId.value ?: return
        val amountMinor = ((amount.toDoubleOrNull() ?: return) * 100).toLong()
        if (amountMinor <= 0) return
        viewModelScope.launch {
            repository.addTransaction(
                LoanTransaction(
                    id = UUID.randomUUID().toString(),
                    personId = id,
                    amountMinor = amountMinor,
                    type = type,
                    source = source,
                    transactionDate = date,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch { repository.deleteTransaction(id) }
    }
}
