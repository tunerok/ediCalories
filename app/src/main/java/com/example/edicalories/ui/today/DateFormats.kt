package com.example.edicalories.ui.today

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
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

fun formatEpochDayShort(epochDay: Long): String {
    return DateTimeFormatter.ofPattern("d MMM")
        .withLocale(Locale.getDefault())
        .format(LocalDate.ofEpochDay(epochDay))
}

fun formatYearMonth(yearMonth: YearMonth): String {
    val raw = DateTimeFormatter.ofPattern("LLLL yyyy")
        .withLocale(Locale.getDefault())
        .format(yearMonth)
    return raw.replaceFirstChar { char ->
        if (char.isLowerCase()) {
            char.titlecase(Locale.getDefault())
        } else {
            char.toString()
        }
    }
}

fun formatWeekdayShort(dayOfWeek: DayOfWeek): String {
    return DateTimeFormatter.ofPattern("EE")
        .withLocale(Locale.getDefault())
        .format(dayOfWeek)
}

fun epochDayToUtcMillis(epochDay: Long): Long {
    return epochDay * MILLIS_PER_DAY
}

fun utcMillisToEpochDay(millis: Long): Long {
    return millis / MILLIS_PER_DAY
}
