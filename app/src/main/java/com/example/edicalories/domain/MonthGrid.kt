package com.example.edicalories.domain

import java.time.DayOfWeek
import java.time.YearMonth

object MonthGrid {
    const val DAYS_IN_WEEK: Int = 7
    const val CELL_COUNT: Int = 42

    fun firstEpochDay(yearMonth: YearMonth, firstDayOfWeek: DayOfWeek): Long {
        val firstOfMonth = yearMonth.atDay(1)
        val dayOffset = Math.floorMod(
            firstOfMonth.dayOfWeek.value - firstDayOfWeek.value,
            DAYS_IN_WEEK,
        )
        return firstOfMonth.minusDays(dayOffset.toLong()).toEpochDay()
    }

    fun lastEpochDay(yearMonth: YearMonth, firstDayOfWeek: DayOfWeek): Long {
        return firstEpochDay(yearMonth, firstDayOfWeek) + (CELL_COUNT - 1).toLong()
    }
}
