package com.example.edicalories.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class MonthGridTest {

    @Test
    fun september2026_mondayFirst_startsOnAugust31() {
        val yearMonth = YearMonth.of(2026, 9)
        val first = MonthGrid.firstEpochDay(yearMonth, DayOfWeek.MONDAY)
        val last = MonthGrid.lastEpochDay(yearMonth, DayOfWeek.MONDAY)
        assertEquals(LocalDate.of(2026, 8, 31).toEpochDay(), first)
        assertEquals(first + (MonthGrid.CELL_COUNT - 1).toLong(), last)
        assertEquals(LocalDate.of(2026, 10, 11).toEpochDay(), last)
    }

    @Test
    fun september2026_sundayFirst_startsOnAugust30() {
        val first = MonthGrid.firstEpochDay(YearMonth.of(2026, 9), DayOfWeek.SUNDAY)
        assertEquals(LocalDate.of(2026, 8, 30).toEpochDay(), first)
    }
}
