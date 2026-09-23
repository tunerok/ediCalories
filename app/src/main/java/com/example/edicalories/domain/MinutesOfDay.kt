package com.example.edicalories.domain

import java.time.LocalTime
import java.util.Locale

object MinutesOfDay {
    const val MINUTES_PER_HOUR: Int = 60
    private const val HOURS_PER_DAY: Int = 24
    const val MIN_INCLUSIVE: Int = 0
    const val MAX_INCLUSIVE: Int = (HOURS_PER_DAY * MINUTES_PER_HOUR) - 1

    fun from(time: LocalTime): Int {
        return (time.hour * MINUTES_PER_HOUR) + time.minute
    }

    fun format(minutesOfDay: Int): String {
        val safe = minutesOfDay.coerceIn(MIN_INCLUSIVE, MAX_INCLUSIVE)
        val hours = safe / MINUTES_PER_HOUR
        val minutes = safe % MINUTES_PER_HOUR
        return String.format(Locale.ROOT, "%02d:%02d", hours, minutes)
    }
}
