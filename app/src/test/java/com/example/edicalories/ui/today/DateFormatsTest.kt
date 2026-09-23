package com.example.edicalories.ui.today

import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.YearMonth

class DateFormatsTest {

    @Test
    fun weekdayShort_formatsAllDays() {
        for (day in DayOfWeek.entries) {
            val label = formatWeekdayShort(day)
            assertTrue(label.isNotBlank())
        }
    }

    @Test
    fun yearMonth_containsYear() {
        val label = formatYearMonth(YearMonth.of(2026, 9))
        assertTrue(label.contains("2026"))
    }
}
