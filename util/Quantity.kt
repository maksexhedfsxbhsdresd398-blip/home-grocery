package com.example.grocerynative.util

fun normalizeToKgOrL(value: Double, unit: String): Double =
    when (unit.lowercase()) {
        "g" -> value / 1000.0
        "ml" -> value / 1000.0
        else -> value
    }

fun splitQuantity(qty: String): Pair<Double, String> {
    val parts = qty.trim().split(" ")
    val v = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
    val u = parts.getOrNull(1) ?: "item"
    return v to u
}
