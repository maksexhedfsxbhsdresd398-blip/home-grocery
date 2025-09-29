package com.example.grocerynative.util

/** Parse "2 kg" -> (2.0, "kg"). Robust to extra spaces or bad input. */
fun splitQuantity(input: String): Pair<Double, String> {
    val parts = input.trim().split(Regex("\\s+"))
    val value = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
    val unit = parts.getOrNull(1)?.lowercase() ?: ""
    return value to unit
}

/** Normalize to a comparable base (kg / L) where it makes sense; for counts, return as-is. */
fun normalizeToKgOrL(value: Double, unit: String): Double = when (unit.lowercase()) {
    "kg" -> value
    "g", "gram", "grams" -> value / 1000.0
    "l", "liter", "litre" -> value
    "ml" -> value / 1000.0
    // counts (packs, dozen, pieces) — treat as counts; the “unit price” is per count
    "pack", "dozen", "pcs", "piece", "pieces" -> value
    else -> value
}
