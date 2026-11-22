package com.expensetracker.app.utils

object NotificationParser {

    // UPI / payment / SMS packages to monitor
    val ALLOWED_PACKAGES = setOf(
        "com.google.android.apps.nbu.paisa.user",   // Google Pay
        "com.phonepe.app",                           // PhonePe
        "net.one97.paytm",                           // Paytm
        "in.org.npci.upiapp",                        // BHIM
        "com.whatsapp",                              // WhatsApp Pay
        "com.amazon.mShop.android.shopping",         // Amazon Pay
        "com.mobikwik_new",                          // MobiKwik
        "com.freecharge.android",                    // FreeCharge
        // SMS apps
        "com.android.mms",
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.android.messaging",
        "com.miui.sms",
        "com.sonyericsson.conversations",
        "org.thoughtcrime.securesms",                // Signal
        // Bank apps
        "com.csam.icici.bank.imobile",
        "com.sbi.lotusintouch",
        "com.axis.mobile",
        "com.hdfcbank.mobilebanking",
        "com.kotak.mahindra.kotak.bank.android",
        "com.rbl.rblmobilebanking",
        "com.idbi.mPassbook",
        "com.snapwork.hdfc",
        "com.barclays.android.barclaysmobilebanking"
    )

    // Debit indicators
    private val DEBIT_REGEX = Regex(
        """(?i)(paid|debited|sent|spent|transferred|payment\s+of|deducted)"""
    )

    // Credit / spam indicators to IGNORE
    private val IGNORE_REGEX = Regex(
        """(?i)(credited|received|refund|otp|request|declined|failed|cashback|reward|balance\s+is|available\s+balance)"""
    )

    // Amount extraction: ₹20, ₹ 20.00, Rs.20, Rs. 20.50, INR 20
    private val AMOUNT_REGEX = Regex(
        """(?:₹|Rs\.?|INR)\s*([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    data class ParsedPayment(
        val amount: Double,
        val merchantOrSource: String
    )

    fun parse(title: String, text: String, appLabel: String): ParsedPayment? {
        val combined = "$title $text"

        // Must have a debit indicator
        if (!DEBIT_REGEX.containsMatchIn(combined)) return null

        // Must NOT have ignore indicators
        if (IGNORE_REGEX.containsMatchIn(combined)) return null

        // Extract amount
        val amountMatch = AMOUNT_REGEX.find(combined) ?: return null
        val amountStr = amountMatch.groupValues[1].replace(",", "")
        val amount = amountStr.toDoubleOrNull() ?: return null
        if (amount <= 0) return null

        // Extract merchant/payee
        val merchant = extractMerchant(combined, appLabel)

        return ParsedPayment(amount, merchant)
    }

    private fun extractMerchant(text: String, appLabel: String): String {
        // Try "to <Name>" pattern
        val toPattern = Regex("""(?i)\bto\s+([A-Za-z0-9&\s\-]{2,30})""")
        val toMatch = toPattern.find(text)
        if (toMatch != null) {
            val candidate = toMatch.groupValues[1].trim()
            if (candidate.isNotBlank() && candidate.length > 1) {
                return candidate.take(30)
            }
        }

        // Try "at <Name>" pattern
        val atPattern = Regex("""(?i)\bat\s+([A-Za-z0-9&\s\-]{2,30})""")
        val atMatch = atPattern.find(text)
        if (atMatch != null) {
            val candidate = atMatch.groupValues[1].trim()
            if (candidate.isNotBlank() && candidate.length > 1) {
                return candidate.take(30)
            }
        }

        return appLabel.ifBlank { "Unknown" }
    }
}
