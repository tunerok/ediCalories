package com.example.edicalories.ui.today

import com.example.edicalories.data.Meal
import com.example.edicalories.domain.CalorieBalance
import com.example.edicalories.domain.ChartPeriod
import com.example.edicalories.domain.MealSchedule
import com.example.edicalories.domain.ProgressRange
import java.time.LocalDate

data class DaySnapshot(
    val epochDay: Long = LocalDate.now().toEpochDay(),
    val dailyGoal: Int = CalorieBalance.DEFAULT_DAILY_GOAL,
    val meals: List<Meal> = emptyList(),
    val consumed: Int = 0,
    val remaining: Int = CalorieBalance.DEFAULT_DAILY_GOAL,
    val progress: Float = 0f,
    val isToday: Boolean = true,
    val priorWeightTenths: Int? = null,
) {
    val isOver: Boolean
        get() = remaining < 0
}

data class ChartPoint(
    val epochDay: Long,
    val value: Float,
)

data class ProgressUiState(
    val period: ChartPeriod = ChartPeriod.Days30,
    val fromEpochDay: Long = LocalDate.now().toEpochDay() - (ProgressRange.DAYS_30 - 1L),
    val toEpochDay: Long = LocalDate.now().toEpochDay(),
    val dailyGoal: Int = CalorieBalance.DEFAULT_DAILY_GOAL,
    val caloriePoints: List<ChartPoint> = emptyList(),
    val weightPoints: List<ChartPoint> = emptyList(),
) {
    val hasCalorieRecords: Boolean
        get() = caloriePoints.any { point -> point.value > 0f }

    val hasWeightRecords: Boolean
        get() = weightPoints.isNotEmpty()
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
    val mealSchedule: MealSchedule = MealSchedule.DEFAULT,
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
    data object InvalidMealWindows : UserMessage
    data object InvalidWeight : UserMessage
    data object JournalCleared : UserMessage
    data object JournalExported : UserMessage
    data object JournalImported : UserMessage
    data object InvalidImportFormat : UserMessage
    data object FileReadError : UserMessage
    data object FileWriteError : UserMessage
    data object WriteError : UserMessage
}
