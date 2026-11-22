package com.expensetracker.app

import android.app.Application
import com.expensetracker.app.data.db.ExpenseDatabase

class ExpenseTrackerApp : Application() {
    val database: ExpenseDatabase by lazy {
        ExpenseDatabase.getDatabase(this)
    }
}
