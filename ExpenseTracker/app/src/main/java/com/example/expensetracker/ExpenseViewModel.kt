package com.example.expensetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ExpenseUiState(
    val name: String = "",
    val amount: String = "",
    val showError: Boolean = false,
    val list: List<Expense> = emptyList()
) {
    val amountError: Boolean get() = showError && (amount.toLongOrNull() ?: 0L) <= 0L
    val total: Long get() = list.sumOf { it.amount }
}

class ExpenseViewModel(
    private val repo: ExpenseRepository = ExpenseRepository
) : ViewModel() {

    private val _inputState = MutableStateFlow(ExpenseUiState())

    val uiState: StateFlow<ExpenseUiState> = combine(_inputState, repo.expenses) { input, list ->
        input.copy(list = list)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseUiState(list = repo.expenses.value)
    )

    fun onNameChange(value: String) {
        _inputState.update { it.copy(name = value, showError = false) }
    }

    fun onAmountChange(value: String) {
        _inputState.update { it.copy(amount = value, showError = false) }
    }

    fun onSubmit() {
        val state = _inputState.value
        val num = state.amount.toLongOrNull() ?: 0L

        if (state.name.isBlank() || num <= 0L) {
            _inputState.update { it.copy(showError = true) }
            return
        }

        repo.add(Expense(name = state.name.trim(), amount = num))
        _inputState.update { it.copy(name = "", amount = "", showError = false) }
    }

    fun deleteExpense(id: String) {
        repo.deleteAt(id)
    }

    fun updateAmount(id: String, newAmount: Long) {
        repo.updateAmount(id, newAmount)
    }
}