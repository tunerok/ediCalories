package com.example.edicalories.ui.today

import com.example.edicalories.data.Meal
import com.example.edicalories.domain.CalorieBalance
import java.time.LocalDate

data class TodayUiState(
    val selectedEpochDay: Long = LocalDate.now().toEpochDay(),
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

sealed interface UserMessage {
    data class Added(val calories: Int) : UserMessage
    data object Saved : UserMessage
    data object Deleted : UserMessage
    data object InvalidCalories : UserMessage
    data object InvalidGoal : UserMessage
    data object WriteError : UserMessage
}
