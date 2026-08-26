@file:OptIn(ExperimentalTime::class)

package com.kotlinfoundation.koko.util.extensions

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Day-first date format, e.g. `07.03.2026`. */
val DayMonthYearFormat: DateTimeFormat<LocalDate> = LocalDate.Format {
    day()
    char('.')
    monthNumber()
    char('.')
    year()
}

/** Current wall-clock time in epoch milliseconds. */
fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()

fun Long.asFormattedDate(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    format: DateTimeFormat<LocalDate> = DayMonthYearFormat,
): String = format.format(Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone).date)

fun Long.asRelativeTimeString(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val now = Clock.System.now()
    val timestamp = Instant.fromEpochMilliseconds(this)
    val nowDateTime = now.toLocalDateTime(timeZone)
    val timestampDateTime = timestamp.toLocalDateTime(timeZone)

    val daysDiff = timestampDateTime.date.toEpochDays() - nowDateTime.date.toEpochDays()

    return when {
        daysDiff == 0L -> "Today"
        daysDiff == 1L -> "Tomorrow"
        daysDiff == -1L -> "Yesterday"
        daysDiff in 2L..6L -> "In $daysDiff days"
        daysDiff in -6L..-2L -> "${-daysDiff} days ago"
        daysDiff in 7L..13L -> "In 1 week"
        daysDiff in -13L..-7L -> "1 week ago"
        daysDiff in 14L..29L -> "In ${daysDiff / 7} weeks"
        daysDiff in -29L..-14L -> "${-daysDiff / 7} weeks ago"
        daysDiff in 30L..59L -> "In 1 month"
        daysDiff in -59L..-30L -> "1 month ago"
        daysDiff >= 60L -> "In ${daysDiff / 30} months"
        daysDiff <= -60L -> "${-daysDiff / 30} months ago"
        else -> this.asFormattedDate(timeZone)
    }
}
