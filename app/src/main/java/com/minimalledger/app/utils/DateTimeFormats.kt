package com.minimalledger.app.utils

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val chinaLocale = Locale.SIMPLIFIED_CHINESE
private val dateFormatter = DateTimeFormatter.ofPattern("M'\u6708'd'\u65E5' EEEE", chinaLocale)
private val monthDayFormatter = DateTimeFormatter.ofPattern("M/d", chinaLocale)
private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy'\u5E74'M'\u6708'", chinaLocale)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", chinaLocale)

fun Instant.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
    return atZone(zoneId).toLocalDate()
}

fun formatLedgerDate(date: LocalDate): String {
    return when (date) {
        LocalDate.now() -> "\u4ECA\u5929"
        LocalDate.now().minusDays(1) -> "\u6628\u5929"
        else -> date.format(dateFormatter)
    }
}

fun formatMonthDay(date: LocalDate): String = date.format(monthDayFormatter)

fun formatYearMonth(month: YearMonth): String = month.format(yearMonthFormatter)

fun formatTime(instant: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String {
    return instant.atZone(zoneId).format(timeFormatter)
}
