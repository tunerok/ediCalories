package com.example.edicalories.domain

data class WeightSample(
    val epochDay: Long,
    val kilograms: Float,
)

data class WeightChartLine(
    val vertices: List<WeightSample> = emptyList(),
    val marks: List<WeightSample> = emptyList(),
)

object WeightChart {
    fun line(
        fromEpochDay: Long,
        toEpochDay: Long,
        samples: List<WeightSample>,
        prior: WeightSample?,
    ): WeightChartLine {
        if (fromEpochDay > toEpochDay) {
            return WeightChartLine()
        }
        val byDay = HashMap<Long, WeightSample>()
        for (sample in samples) {
            if (sample.epochDay < fromEpochDay || sample.epochDay > toEpochDay) {
                continue
            }
            byDay[sample.epochDay] = sample
        }
        val inRange = byDay.values.sortedBy { sample -> sample.epochDay }
        if (inRange.isEmpty()) {
            val carried = prior?.takeIf { entry -> entry.epochDay < fromEpochDay } ?: return WeightChartLine()
            return WeightChartLine(
                vertices = hold(fromEpochDay, toEpochDay, carried.kilograms),
            )
        }
        val vertices = ArrayList<WeightSample>(inRange.size + 2)
        val first = inRange[0]
        if (first.epochDay > fromEpochDay) {
            vertices.add(WeightSample(fromEpochDay, first.kilograms))
        }
        vertices.addAll(inRange)
        val last = inRange[inRange.size - 1]
        if (last.epochDay < toEpochDay) {
            vertices.add(WeightSample(toEpochDay, last.kilograms))
        }
        return WeightChartLine(
            vertices = vertices,
            marks = inRange,
        )
    }

    private fun hold(fromEpochDay: Long, toEpochDay: Long, kilograms: Float): List<WeightSample> {
        if (fromEpochDay == toEpochDay) {
            return listOf(WeightSample(fromEpochDay, kilograms))
        }
        return listOf(
            WeightSample(fromEpochDay, kilograms),
            WeightSample(toEpochDay, kilograms),
        )
    }
}
