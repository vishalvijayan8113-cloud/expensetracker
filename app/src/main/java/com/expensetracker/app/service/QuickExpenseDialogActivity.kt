package com.expensetracker.app.service

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color
import com.expensetracker.app.ui.screens.QuickExpensePopup
import com.expensetracker.app.ui.theme.ExpenseTrackerTheme

class QuickExpenseDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val amount = intent.getDoubleExtra(PaymentNotificationListenerService.EXTRA_AMOUNT, 0.0)
        val merchant = intent.getStringExtra(PaymentNotificationListenerService.EXTRA_MERCHANT) ?: "Unknown"
        val rawText = intent.getStringExtra(PaymentNotificationListenerService.EXTRA_RAW_TEXT) ?: ""

        if (amount <= 0) {
            finish()
            return
        }

        setContent {
            ExpenseTrackerTheme {
                QuickExpensePopup(
                    amount = amount,
                    merchant = merchant,
                    rawNotificationText = rawText,
                    onDismiss = { finish() }
                )
            }
        }
    }
}
