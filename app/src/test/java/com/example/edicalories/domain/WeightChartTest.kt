package com.example.edicalories.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeightChartTest {

    @Test
    fun line_keepsSlopeBetweenSamplesAndShelvesAtPlotEnds() {
        val line = WeightChart.line(
            fromEpochDay = 0L,
            toEpochDay = 10L,
            samples = listOf(
                WeightSample(2L, 80f),
                WeightSample(6L, 70f),
            ),
            prior = null,
        )

        assertEquals(
            listOf(
                WeightSample(0L, 80f),
                WeightSample(2L, 80f),
                WeightSample(6L, 70f),
                WeightSample(10L, 70f),
            ),
            line.vertices,
        )
        assertEquals(
            listOf(
                WeightSample(2L, 80f),
                WeightSample(6L, 70f),
            ),
            line.marks,
        )
    }

    @Test
    fun line_staysFlatWhereWeightDidNotChange() {
        val line = WeightChart.line(
            fromEpochDay = 0L,
            toEpochDay = 10L,
            samples = listOf(
                WeightSample(2L, 80f),
                WeightSample(6L, 80f),
            ),
            prior = null,
        )

        assertEquals(80f, line.vertices[0].kilograms)
        assertEquals(80f, line.vertices[1].kilograms)
        assertEquals(80f, line.vertices[2].kilograms)
        assertEquals(80f, line.vertices[3].kilograms)
    }

    @Test
    fun line_doesNotAddShelfWhenSampleIsAlreadyOnTheEdge() {
        val line = WeightChart.line(
            fromEpochDay = 0L,
            toEpochDay = 10L,
            samples = listOf(
                WeightSample(0L, 80f),
                WeightSample(10L, 75f),
            ),
            prior = WeightSample(-4L, 90f),
        )

        assertEquals(
            listOf(
                WeightSample(0L, 80f),
                WeightSample(10L, 75f),
            ),
            line.vertices,
        )
    }

    @Test
    fun line_holdsPriorWeightAcrossPlotWhenNothingWasLoggedInside() {
        val line = WeightChart.line(
            fromEpochDay = 5L,
            toEpochDay = 12L,
            samples = emptyList(),
            prior = WeightSample(1L, 82f),
        )

        assertEquals(
            listOf(
                WeightSample(5L, 82f),
                WeightSample(12L, 82f),
            ),
            line.vertices,
        )
        assertTrue(line.marks.isEmpty())
    }

    @Test
    fun line_isEmptyWithoutAnyWeight() {
        val line = WeightChart.line(
            fromEpochDay = 0L,
            toEpochDay = 10L,
            samples = emptyList(),
            prior = null,
        )

        assertTrue(line.vertices.isEmpty())
        assertTrue(line.marks.isEmpty())
    }
}
