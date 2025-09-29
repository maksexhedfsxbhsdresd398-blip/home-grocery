package com.example.grocerynative.util

fun splitQuantity(q: String): Pair<Double, String> {
    val parts = q.trim().split(" ")
    val v = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
    val u = parts.getOrNull(1)?.lowercase() ?: ""
    return v to u
}

fun normalizeToKgOrL(value: Double, unit: String): Double =
    when (unit.lowercase()) {
        "kg" -> value
        "g", "gram", "grams" -> value / 1000.0
        "l", "liter", "litre" -> value
        "ml" -> value / 1000.0
        // counts just count
        "pack", "dozen", "pcs", "piece", "pieces" -> value
        else -> value
    }
