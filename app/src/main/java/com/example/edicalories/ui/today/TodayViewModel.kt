package com.example.edicalories.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.edicalories.data.Meal
import com.example.edicalories.data.MealRepository
import com.example.edicalories.data.PreferencesRepository
import com.example.edicalories.domain.CalorieBalance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val mealRepository: MealRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val selectedEpochDay = MutableStateFlow(LocalDate.now().toEpochDay())

    private val messagesChannel = Channel<UserMessage>(Channel.BUFFERED)
    val messages = messagesChannel.receiveAsFlow()

    private val mealsForSelectedDay = selectedEpochDay.flatMapLatest { epochDay ->
        mealRepository.observeMealsForDay(epochDay)
    }

    val uiState: StateFlow<TodayUiState> = combine(
        selectedEpochDay,
        mealsForSelectedDay,
        preferencesRepository.dailyGoal,
    ) { epochDay, meals, dailyGoal ->
        val consumed = CalorieBalance.consumed(meals.map { meal -> meal.calories })
        TodayUiState(
            selectedEpochDay = epochDay,
            dailyGoal = dailyGoal,
            meals = meals,
            consumed = consumed,
            remaining = CalorieBalance.remaining(dailyGoal, consumed),
            progress = CalorieBalance.progress(dailyGoal, consumed),
            isToday = epochDay == LocalDate.now().toEpochDay(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState(),
    )

    fun selectPreviousDay() {
        selectedEpochDay.value = selectedEpochDay.value - 1L
    }

    fun selectNextDay() {
        selectedEpochDay.value = selectedEpochDay.value + 1L
    }

    fun selectToday() {
        selectedEpochDay.value = LocalDate.now().toEpochDay()
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

    fun setDailyGoal(goalRaw: String) {
        val goal = CalorieBalance.parseDailyGoal(goalRaw)
        if (goal == null) {
            emitMessage(UserMessage.InvalidGoal)
            return
        }
        viewModelScope.launch {
            runCatching {
                preferencesRepository.setDailyGoal(goal)
            }.onSuccess {
                emitMessage(UserMessage.Saved)
            }.onFailure {
                emitMessage(UserMessage.WriteError)
            }
        }
    }

    private fun addMeal(calories: Int, epochDay: Long) {
        viewModelScope.launch {
            runCatching {
                mealRepository.add(calories, epochDay)
            }.onSuccess {
                emitMessage(UserMessage.Added(calories))
            }.onFailure {
                emitMessage(UserMessage.WriteError)
            }
        }
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
