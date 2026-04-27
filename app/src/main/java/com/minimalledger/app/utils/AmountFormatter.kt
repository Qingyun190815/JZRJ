package com.minimalledger.app.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

private val currencyFormat = DecimalFormat("\u00A5#,##0.00")

fun parseAmountInputToCents(input: String): Long? {
    if (input.isBlank()) {
        return null
    }
    val parsed = input.toBigDecimalOrNull() ?: return null
    if (parsed <= BigDecimal.ZERO) {
        return null
    }
    return parsed
        .multiply(BigDecimal(100))
        .setScale(0, RoundingMode.HALF_UP)
        .longValueExact()
}

fun sanitizeAmountInput(input: String): String {
    val builder = StringBuilder()
    var hasDecimalPoint = false
    var decimalPlaces = 0

    input.forEach { char ->
        when {
            char.isDigit() && !hasDecimalPoint -> {
                builder.append(char)
            }
            char.isDigit() && decimalPlaces < 2 -> {
                builder.append(char)
                decimalPlaces += 1
            }
            char == '.' && !hasDecimalPoint -> {
                if (builder.isEmpty()) {
                    builder.append('0')
                }
                builder.append('.')
                hasDecimalPoint = true
            }
        }
    }

    return builder.toString().take(12)
}

fun formatCurrency(cents: Long): String {
    return currencyFormat.format(BigDecimal(cents).divide(BigDecimal(100)))
}

fun formatAmountInput(cents: Long): String {
    val yuan = cents / 100
    val fen = kotlin.math.abs(cents % 100)
    return if (fen == 0L) {
        yuan.toString()
    } else {
        "$yuan.${fen.toString().padStart(2, '0')}"
    }
}

fun formatSignedCurrency(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    return sign + formatCurrency(kotlin.math.abs(cents))
}
