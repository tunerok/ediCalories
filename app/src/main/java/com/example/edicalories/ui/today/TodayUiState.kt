package com.example.edicalories.ui.today

import com.example.edicalories.data.Meal
import com.example.edicalories.domain.CalorieBalance
import java.time.LocalDate

data class DaySnapshot(
    val epochDay: Long = LocalDate.now().toEpochDay(),
    val dailyGoal: Int = CalorieBalance.DEFAULT_DAILY_GOAL,
    val meals: List<Meal> = emptyList(),
    val consumed: Int = 0,
    val remaining: Int = CalorieBalance.DEFAULT_DAILY_GOAL,
    val progress: Float = 0f,
    val isToday: Boolean = true,
) {
    val isOver: Boolean
        get() = remaining < 0
}

data class TodayUiState(
    val previous: DaySnapshot = DaySnapshot(
        epochDay = LocalDate.now().toEpochDay() - 1L,
        isToday = false,
    ),
    val current: DaySnapshot = DaySnapshot(),
    val next: DaySnapshot = DaySnapshot(
        epochDay = LocalDate.now().toEpochDay() + 1L,
        isToday = false,
    ),
) {
    val selectedEpochDay: Long
        get() = current.epochDay

    val dailyGoal: Int
        get() = current.dailyGoal

    val isToday: Boolean
        get() = current.isToday
}

sealed interface UserMessage {
    data class Added(val calories: Int) : UserMessage
    data object Saved : UserMessage
    data object Deleted : UserMessage
    data object InvalidCalories : UserMessage
    data object InvalidGoal : UserMessage
    data object WriteError : UserMessage
}
