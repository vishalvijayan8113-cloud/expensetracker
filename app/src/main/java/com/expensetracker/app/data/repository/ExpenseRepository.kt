package com.expensetracker.app.data.repository

import com.expensetracker.app.data.db.CategoryTotal
import com.expensetracker.app.data.db.Expense
import com.expensetracker.app.data.db.ExpenseDao
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getTodayTotal(): Flow<Double?> {
        val startOfDay = getStartOfDay()
        return expenseDao.getTodayTotal(startOfDay)
    }

    fun getMonthTotal(): Flow<Double?> {
        val startOfMonth = getStartOfMonth()
        return expenseDao.getMonthTotal(startOfMonth)
    }

    fun getCategoryBreakdown(): Flow<List<CategoryTotal>> {
        val startOfMonth = getStartOfMonth()
        return expenseDao.getCategoryBreakdown(startOfMonth)
    }

    suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun isDuplicate(amount: Double): Boolean {
        val now = System.currentTimeMillis()
        return expenseDao.countRecentDuplicates(amount, now) > 0
    }

    private fun getStartOfDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
