package com.expensetracker.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.data.db.CategoryTotal
import com.expensetracker.app.data.db.Expense
import com.expensetracker.app.data.db.ExpenseDatabase
import com.expensetracker.app.data.repository.ExpenseRepository
import com.expensetracker.app.utils.CategoryMapper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val todayTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val categoryBreakdown: List<CategoryTotal> = emptyList(),
    val isLoading: Boolean = true
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        val db = ExpenseDatabase.getDatabase(application)
        repository = ExpenseRepository(db.expenseDao())

        viewModelScope.launch {
            combine(
                repository.allExpenses,
                repository.getTodayTotal(),
                repository.getMonthTotal(),
                repository.getCategoryBreakdown()
            ) { expenses, todayTotal, monthTotal, breakdown ->
                ExpenseUiState(
                    expenses = expenses,
                    todayTotal = todayTotal ?: 0.0,
                    monthTotal = monthTotal ?: 0.0,
                    categoryBreakdown = breakdown,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addExpense(
        amount: Double,
        note: String,
        category: String? = null,
        merchantOrSource: String = "",
        rawNotificationText: String = ""
    ) {
        val resolvedCategory = category ?: CategoryMapper.categorize(note)
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    amount = amount,
                    note = note,
                    category = resolvedCategory,
                    merchantOrSource = merchantOrSource,
                    timestamp = System.currentTimeMillis(),
                    rawNotificationText = rawNotificationText
                )
            )
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun deleteExpenseById(id: Long) {
        viewModelScope.launch {
            repository.deleteExpenseById(id)
        }
    }
}
