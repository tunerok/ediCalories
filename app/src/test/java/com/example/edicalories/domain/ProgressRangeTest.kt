package com.example.edicalories.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressRangeTest {

    @Test
    fun startEpochDay_usesInclusiveWindowForFixedPeriods() {
        val today = 10_000L
        assertEquals(today - 6L, ProgressRange.startEpochDay(ChartPeriod.Days7, today, null))
        assertEquals(today - 29L, ProgressRange.startEpochDay(ChartPeriod.Days30, today, null))
        assertEquals(today - 89L, ProgressRange.startEpochDay(ChartPeriod.Days90, today, null))
    }

    @Test
    fun startEpochDay_allUsesEarliestRecord() {
        val today = 10_000L
        assertEquals(today, ProgressRange.startEpochDay(ChartPeriod.All, today, null))
        assertEquals(9_900L, ProgressRange.startEpochDay(ChartPeriod.All, today, 9_900L))
    }

    @Test
    fun startEpochDay_doesNotGoPastToday() {
        val today = 10_000L
        assertEquals(today, ProgressRange.startEpochDay(ChartPeriod.All, today, 10_050L))
    }
}
