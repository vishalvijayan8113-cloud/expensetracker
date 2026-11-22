package com.expensetracker.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getExpensesFromTimestamp(startOfDay: Long): Flow<List<Expense>>

    /**
     * Deduplication check: returns count of expenses with same amount
     * recorded within the last 3 minutes (180,000 ms).
     */
    @Query(
        "SELECT COUNT(*) FROM expenses WHERE amount = :amount AND timestamp > (:now - 180000)"
    )
    suspend fun countRecentDuplicates(amount: Double, now: Long): Int

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startOfDay")
    fun getTodayTotal(startOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startOfMonth")
    fun getMonthTotal(startOfMonth: Long): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE timestamp >= :startOfMonth GROUP BY category ORDER BY total DESC")
    fun getCategoryBreakdown(startOfMonth: Long): Flow<List<CategoryTotal>>
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
