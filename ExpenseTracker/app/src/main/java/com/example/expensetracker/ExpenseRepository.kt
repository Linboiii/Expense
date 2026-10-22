package com.example.expensetracker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object ExpenseRepository {
    private val _expenses = MutableStateFlow(
        listOf(
            Expense(name = "Makan Nasi", amount = 90000),
            Expense(name = "Investasi", amount = 10000000),
            Expense(name = "Kopi", amount = 18000)
        )
    )
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    fun add(expense: Expense) {
        _expenses.update { listOf(expense) + it }
    }

    fun deleteAt(id: String) {
        _expenses.update { list ->
            list.filterNot { it.id == id }
        }
    }

    fun updateAmount(id: String, amount: Long) {
        _expenses.update { list ->
            list.map { if (it.id == id) it.copy(amount = amount) else it }
        }
    }
}