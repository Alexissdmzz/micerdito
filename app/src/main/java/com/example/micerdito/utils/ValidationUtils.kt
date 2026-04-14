package com.example.micerdito.utils


fun parsePositiveAmount(input: String): Double? {
    val normalized = input
        .trim()
        .replace(",", ".")

    val value = normalized.toDoubleOrNull() ?: return null

    return if (value > 0) value else null
}
