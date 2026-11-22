package com.expensetracker.app.utils

object CategoryMapper {

    private val FOOD_DRINKS = setOf(
        "water", "tea", "coffee", "lunch", "dinner", "food", "snacks",
        "biryani", "juice", "eat", "meal", "breakfast", "restaurant",
        "pizza", "burger", "sandwich", "chai", "hotel", "cafe", "swiggy",
        "zomato", "dominos", "kfc", "mcdonalds"
    )

    private val TRAVEL_FUEL = setOf(
        "petrol", "diesel", "auto", "uber", "ola", "bus", "metro",
        "fuel", "parking", "cab", "taxi", "train", "flight", "rapido",
        "rickshaw", "toll", "namma", "bmtc", "irctc", "redbus"
    )

    private val GROCERIES = setOf(
        "milk", "vegetables", "grocery", "supermarket", "fruits",
        "bread", "eggs", "rice", "dal", "sugar", "salt", "big bazaar",
        "dmart", "reliance fresh", "blinkit", "zepto", "instamart",
        "vegetable", "sabji", "market", "mandi"
    )

    private val BILLS_UTILITIES = setOf(
        "recharge", "electricity", "wifi", "bill", "rent", "water bill",
        "gas", "phone", "internet", "postpaid", "prepaid", "dth",
        "broadband", "subscription", "netflix", "amazon prime",
        "hotstar", "jio", "airtel", "bsnl", "vodafone"
    )

    private val SHOPPING = setOf(
        "amazon", "flipkart", "myntra", "ajio", "nykaa", "meesho",
        "clothes", "shirt", "shoes", "dress", "accessories", "watch",
        "mobile", "laptop", "electronics", "gadget", "book", "pharmacy",
        "medicine", "medical", "chemist"
    )

    fun categorize(note: String): String {
        val lower = note.lowercase().trim()

        if (FOOD_DRINKS.any { lower.contains(it) }) return "Food & Drinks"
        if (TRAVEL_FUEL.any { lower.contains(it) }) return "Travel & Fuel"
        if (GROCERIES.any { lower.contains(it) }) return "Groceries"
        if (BILLS_UTILITIES.any { lower.contains(it) }) return "Bills & Utilities"
        if (SHOPPING.any { lower.contains(it) }) return "Shopping"

        return "Other"
    }

    val allCategories = listOf(
        "Food & Drinks",
        "Travel & Fuel",
        "Groceries",
        "Bills & Utilities",
        "Shopping",
        "Other"
    )

    fun categoryIcon(category: String): String {
        return when (category) {
            "Food & Drinks" -> "🍽️"
            "Travel & Fuel" -> "🚗"
            "Groceries" -> "🛒"
            "Bills & Utilities" -> "💡"
            "Shopping" -> "🛍️"
            else -> "💸"
        }
    }
}
