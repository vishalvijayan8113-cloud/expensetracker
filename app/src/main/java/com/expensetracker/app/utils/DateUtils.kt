package com.expensetracker.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatAmount(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) {
            "₹${amount.toLong()}"
        } else {
            "₹%.2f".format(amount)
        }
    }
}
