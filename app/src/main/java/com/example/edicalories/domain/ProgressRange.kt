package com.example.edicalories.domain

enum class ChartPeriod {
    Days7,
    Days30,
    Days90,
    All,
}

object ProgressRange {
    const val DAYS_7: Int = 7
    const val DAYS_30: Int = 30
    const val DAYS_90: Int = 90
    const val MAX_CHART_DAYS: Long = 20L * 366L

    fun startEpochDay(
        period: ChartPeriod,
        todayEpochDay: Long,
        earliestEpochDay: Long?,
    ): Long {
        val computed = when (period) {
            ChartPeriod.Days7 -> todayEpochDay - (DAYS_7 - 1L)
            ChartPeriod.Days30 -> todayEpochDay - (DAYS_30 - 1L)
            ChartPeriod.Days90 -> todayEpochDay - (DAYS_90 - 1L)
            ChartPeriod.All -> earliestEpochDay ?: todayEpochDay
        }
        val boundedStart = minOf(computed, todayEpochDay)
        val maxSpanStart = todayEpochDay - (MAX_CHART_DAYS - 1L)
        return maxOf(boundedStart, maxSpanStart)
    }
}
