package com.minimalledger.app.utils

import com.minimalledger.app.data.model.Transaction
import com.minimalledger.app.data.model.TransactionType
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val csvDateTimeFormatter = DateTimeFormatter.ofPattern(
    "yyyy-MM-dd HH:mm:ss",
    Locale.SIMPLIFIED_CHINESE,
)

fun transactionsToCsv(
    transactions: List<Transaction>,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val header = listOf("id", "amount", "type", "note", "time", "created_at", "updated_at")
        .joinToString(",")
    val rows = transactions
        .sortedByDescending { it.timestamp }
        .map { transaction ->
            listOf(
                transaction.id.toString(),
                centsToPlainAmount(transaction.amountInCents),
                if (transaction.type == TransactionType.INCOME) "income" else "expense",
                transaction.note,
                transaction.timestamp.atZone(zoneId).format(csvDateTimeFormatter),
                transaction.createdAt.atZone(zoneId).format(csvDateTimeFormatter),
                transaction.updatedAt.atZone(zoneId).format(csvDateTimeFormatter),
            ).joinToString(",") { it.csvEscaped() }
        }
    return (listOf(header) + rows).joinToString("\n")
}

private fun centsToPlainAmount(cents: Long): String {
    val yuan = cents / 100
    val fen = kotlin.math.abs(cents % 100).toString().padStart(2, '0')
    return "$yuan.$fen"
}

private fun String.csvEscaped(): String {
    val escaped = replace("\"", "\"\"")
    return if (any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
        "\"$escaped\""
    } else {
        escaped
    }
}
