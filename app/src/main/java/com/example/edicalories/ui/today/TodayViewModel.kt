package com.example.edicalories.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.edicalories.data.DayTotal
import com.example.edicalories.data.Meal
import com.example.edicalories.data.MealRepository
import com.example.edicalories.data.PreferencesRepository
import com.example.edicalories.domain.CalorieBalance
import com.example.edicalories.domain.MealSchedule
import com.example.edicalories.domain.MinutesOfDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val mealRepository: MealRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val selectedEpochDay = MutableStateFlow(LocalDate.now().toEpochDay())

    private val messagesChannel = Channel<UserMessage>(Channel.BUFFERED)
    val messages = messagesChannel.receiveAsFlow()

    private val mealsWindow = selectedEpochDay.flatMapLatest { centerEpochDay ->
        mealRepository.observeMealsForRange(centerEpochDay - 1L, centerEpochDay + 1L)
            .map { meals -> MealsWindow(centerEpochDay, meals) }
    }

    val uiState: StateFlow<TodayUiState> = combine(
        mealsWindow,
        preferencesRepository.dailyGoal,
        preferencesRepository.mealSchedule,
    ) { window, dailyGoal, mealSchedule ->
        val grouped = window.meals.groupBy { meal -> meal.epochDay }
        val todayEpochDay = LocalDate.now().toEpochDay()
        TodayUiState(
            previous = snapshotFor(window.centerEpochDay - 1L, grouped, dailyGoal, todayEpochDay),
            current = snapshotFor(window.centerEpochDay, grouped, dailyGoal, todayEpochDay),
            next = snapshotFor(window.centerEpochDay + 1L, grouped, dailyGoal, todayEpochDay),
            mealSchedule = mealSchedule,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState(),
    )

    private val calendarRange = MutableStateFlow<EpochDayRange?>(null)

    val calendarDayTotals: StateFlow<Map<Long, Int>> = calendarRange
        .flatMapLatest { range ->
            if (range == null) {
                flowOf(emptyMap())
            } else {
                mealRepository.observeDayTotals(range.fromEpochDay, range.toEpochDay)
                    .map { totals -> totalsToMap(totals) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    fun observeCalendarRange(fromEpochDay: Long, toEpochDay: Long) {
        if (toEpochDay < fromEpochDay) {
            return
        }
        calendarRange.value = EpochDayRange(
            fromEpochDay = fromEpochDay,
            toEpochDay = toEpochDay,
        )
    }

    fun stopObservingCalendar() {
        calendarRange.value = null
    }

    fun selectPreviousDay() {
        selectedEpochDay.value = selectedEpochDay.value - 1L
    }

    fun selectNextDay() {
        selectedEpochDay.value = selectedEpochDay.value + 1L
    }

    fun selectToday() {
        selectedEpochDay.value = LocalDate.now().toEpochDay()
    }

    fun selectEpochDay(epochDay: Long) {
        selectedEpochDay.value = epochDay
    }

    fun addQuick(calories: Int) {
        addMeal(calories, selectedEpochDay.value)
    }

    fun addCustom(caloriesRaw: String, epochDay: Long) {
        val calories = CalorieBalance.parsePositiveCalories(caloriesRaw)
        if (calories == null) {
            emitMessage(UserMessage.InvalidCalories)
            return
        }
        addMeal(calories, epochDay)
    }

    fun updateMeal(meal: Meal, caloriesRaw: String, epochDay: Long) {
        val calories = CalorieBalance.parsePositiveCalories(caloriesRaw)
        if (calories == null) {
            emitMessage(UserMessage.InvalidCalories)
            return
        }
        viewModelScope.launch {
            runCatching {
                mealRepository.update(meal.copy(calories = calories, epochDay = epochDay))
            }.onSuccess {
                emitMessage(UserMessage.Saved)
            }.onFailure {
                emitMessage(UserMessage.WriteError)
            }
        }
    }

    fun deleteMeal(meal: Meal) {
        viewModelScope.launch {
            runCatching {
                mealRepository.delete(meal)
            }.onSuccess {
                emitMessage(UserMessage.Deleted)
            }.onFailure {
                emitMessage(UserMessage.WriteError)
            }
        }
    }

    fun saveSettings(goalRaw: String, schedule: MealSchedule) {
        val goal = CalorieBalance.parseDailyGoal(goalRaw)
        if (goal == null) {
            emitMessage(UserMessage.InvalidGoal)
            return
        }
        if (schedule.groupingEnabled && !schedule.isValid()) {
            emitMessage(UserMessage.InvalidMealWindows)
            return
        }
        viewModelScope.launch {
            runCatching {
                preferencesRepository.setDailyGoalAndSchedule(goal, schedule)
            }.onSuccess {
                emitMessage(UserMessage.Saved)
            }.onFailure {
                emitMessage(UserMessage.WriteError)
            }
        }
    }

    private fun addMeal(calories: Int, epochDay: Long) {
        val minutesOfDay = MinutesOfDay.from(LocalTime.now())
        viewModelScope.launch {
            runCatching {
                mealRepository.add(calories, epochDay, minutesOfDay)
            }.onSuccess {
                emitMessage(UserMessage.Added(calories))
            }.onFailure {
                emitMessage(UserMessage.WriteError)
            }
        }
    }

    private fun snapshotFor(
        epochDay: Long,
        grouped: Map<Long, List<Meal>>,
        dailyGoal: Int,
        todayEpochDay: Long,
    ): DaySnapshot {
        val meals = grouped[epochDay].orEmpty()
        val consumed = CalorieBalance.consumed(meals.map { meal -> meal.calories })
        return DaySnapshot(
            epochDay = epochDay,
            dailyGoal = dailyGoal,
            meals = meals,
            consumed = consumed,
            remaining = CalorieBalance.remaining(dailyGoal, consumed),
            progress = CalorieBalance.progress(dailyGoal, consumed),
            isToday = epochDay == todayEpochDay,
        )
    }

    private fun emitMessage(message: UserMessage) {
        viewModelScope.launch {
            messagesChannel.send(message)
        }
    }

    class Factory(
        private val mealRepository: MealRepository,
        private val preferencesRepository: PreferencesRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TodayViewModel(mealRepository, preferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

private data class MealsWindow(
    val centerEpochDay: Long,
    val meals: List<Meal>,
)

private data class EpochDayRange(
    val fromEpochDay: Long,
    val toEpochDay: Long,
)

private fun totalsToMap(totals: List<DayTotal>): Map<Long, Int> {
    val result = HashMap<Long, Int>(totals.size)
    for (total in totals) {
        result[total.epochDay] = total.totalCalories
    }
    return result
}
