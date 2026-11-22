package com.expensetracker.app.service

import android.app.Notification
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.expensetracker.app.data.db.ExpenseDatabase
import com.expensetracker.app.data.repository.ExpenseRepository
import com.expensetracker.app.utils.NotificationParser
import kotlinx.coroutines.*

class PaymentNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: ExpenseRepository
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val TAG = "PaymentNLS"
        const val PREFS_NAME = "expense_tracker_prefs"
        const val KEY_TEST_MODE = "test_mode"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_MERCHANT = "extra_merchant"
        const val EXTRA_RAW_TEXT = "extra_raw_text"
    }

    override fun onCreate() {
        super.onCreate()
        val db = ExpenseDatabase.getDatabase(applicationContext)
        repository = ExpenseRepository(db.expenseDao())
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        Log.d(TAG, "PaymentNotificationListenerService created")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val packageName = sbn.packageName ?: return
        val isTestMode = prefs.getBoolean(KEY_TEST_MODE, false)

        // Package filtering (first line): only process allowed apps unless test mode
        if (!isTestMode) {
            val isAllowed = NotificationParser.ALLOWED_PACKAGES.contains(packageName) ||
                    packageName.contains("messaging", ignoreCase = true) ||
                    packageName.contains("mms", ignoreCase = true) ||
                    packageName.contains("sms", ignoreCase = true)
            if (!isAllowed) return
        }

        val notification: Notification = sbn.notification ?: return
        val extras: Bundle = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: text

        val fullText = "$title $bigText"
        Log.d(TAG, "Processing notification from $packageName: $fullText")

        // Get readable app label
        val appLabel = try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        val parsed = NotificationParser.parse(title, bigText, appLabel) ?: return
        Log.d(TAG, "Parsed payment: amount=${parsed.amount}, merchant=${parsed.merchantOrSource}")

        // Deduplication check
        serviceScope.launch {
            val isDuplicate = repository.isDuplicate(parsed.amount)
            if (isDuplicate) {
                Log.d(TAG, "Duplicate transaction detected for ₹${parsed.amount}, skipping.")
                return@launch
            }

            // Launch Quick Entry Dialog
            withContext(Dispatchers.Main) {
                launchQuickEntryDialog(parsed.amount, parsed.merchantOrSource, fullText)
            }
        }
    }

    private fun launchQuickEntryDialog(amount: Double, merchant: String, rawText: String) {
        val intent = Intent(this, QuickExpenseDialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_AMOUNT, amount)
            putExtra(EXTRA_MERCHANT, merchant)
            putExtra(EXTRA_RAW_TEXT, rawText)
        }
        startActivity(intent)
    }
}
