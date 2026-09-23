package com.example.edicalories.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class MinutesOfDayTest {

    @Test
    fun from_usesHourAndMinuteOnly() {
        assertEquals(0, MinutesOfDay.from(LocalTime.of(0, 0, 59)))
        assertEquals(90, MinutesOfDay.from(LocalTime.of(1, 30)))
        assertEquals(MinutesOfDay.MAX_INCLUSIVE, MinutesOfDay.from(LocalTime.of(23, 59)))
    }

    @Test
    fun format_padsHoursAndMinutes() {
        assertEquals("00:00", MinutesOfDay.format(0))
        assertEquals("01:30", MinutesOfDay.format(90))
        assertEquals("23:59", MinutesOfDay.format(MinutesOfDay.MAX_INCLUSIVE))
    }
}
