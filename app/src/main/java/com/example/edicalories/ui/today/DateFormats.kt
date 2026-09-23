package com.example.edicalories.ui.today

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private const val MILLIS_PER_DAY: Long = 86_400_000L

fun formatEpochDay(epochDay: Long): String {
    val date = LocalDate.ofEpochDay(epochDay)
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
        .format(date)
}

fun epochDayToUtcMillis(epochDay: Long): Long {
    return epochDay * MILLIS_PER_DAY
}

fun utcMillisToEpochDay(millis: Long): Long {
    return millis / MILLIS_PER_DAY
}
