package com.example.edicalories.domain

object CalorieBalance {
    const val MIN_CALORIES: Int = 1
    const val MAX_CALORIES: Int = 20_000
    const val MIN_DAILY_GOAL: Int = 1
    const val MAX_DAILY_GOAL: Int = 20_000
    const val DEFAULT_DAILY_GOAL: Int = 1800

    fun consumed(calories: List<Int>): Int {
        var total = 0L
        for (value in calories) {
            total += value.toLong()
        }
        return total.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
    }

    fun remaining(dailyGoal: Int, consumed: Int): Int {
        return dailyGoal - consumed
    }

    fun progress(dailyGoal: Int, consumed: Int): Float {
        if (dailyGoal <= 0) {
            return 0f
        }
        val ratio = consumed.toFloat() / dailyGoal.toFloat()
        return ratio.coerceIn(0f, 1f)
    }

    fun parsePositiveCalories(raw: String): Int? {
        return parseBoundedInt(raw, MIN_CALORIES, MAX_CALORIES)
    }

    fun parseDailyGoal(raw: String): Int? {
        return parseBoundedInt(raw, MIN_DAILY_GOAL, MAX_DAILY_GOAL)
    }

    private fun parseBoundedInt(raw: String, min: Int, max: Int): Int? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val value = trimmed.toIntOrNull() ?: return null
        if (value < min || value > max) {
            return null
        }
        return value
    }
}
