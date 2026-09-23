package com.example.edicalories.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalorieBalanceTest {

    @Test
    fun remaining_subtractsConsumedFromGoal() {
        assertEquals(1300, CalorieBalance.remaining(1800, 500))
    }

    @Test
    fun remaining_goesNegativeWhenOver() {
        assertEquals(-200, CalorieBalance.remaining(1800, 2000))
    }

    @Test
    fun consumed_sumsCalories() {
        assertEquals(350, CalorieBalance.consumed(listOf(50, 100, 200)))
    }

    @Test
    fun consumed_emptyListIsZero() {
        assertEquals(0, CalorieBalance.consumed(emptyList()))
    }

    @Test
    fun progress_isRatioCappedAtOne() {
        assertEquals(0.5f, CalorieBalance.progress(1800, 900), 0.0001f)
        assertEquals(1f, CalorieBalance.progress(1800, 2000), 0.0001f)
        assertEquals(0f, CalorieBalance.progress(0, 100), 0.0001f)
    }

    @Test
    fun parsePositiveCalories_acceptsValidWholeNumbers() {
        assertEquals(250, CalorieBalance.parsePositiveCalories("250"))
        assertEquals(1, CalorieBalance.parsePositiveCalories(" 1 "))
    }

    @Test
    fun parsePositiveCalories_rejectsInvalidInput() {
        assertNull(CalorieBalance.parsePositiveCalories(""))
        assertNull(CalorieBalance.parsePositiveCalories("abc"))
        assertNull(CalorieBalance.parsePositiveCalories("0"))
        assertNull(CalorieBalance.parsePositiveCalories("-10"))
        assertNull(CalorieBalance.parsePositiveCalories("20001"))
    }

    @Test
    fun parseDailyGoal_usesSameBoundsAsCalories() {
        assertEquals(1800, CalorieBalance.parseDailyGoal("1800"))
        assertNull(CalorieBalance.parseDailyGoal(""))
        assertNull(CalorieBalance.parseDailyGoal("0"))
    }
}
