package com.example.edicalories.ui.today

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.edicalories.data.DayTotal
import com.example.edicalories.data.JournalRepository
import com.example.edicalories.data.Meal
import com.example.edicalories.data.MealRepository
import com.example.edicalories.data.PreferencesRepository
import com.example.edicalories.data.WeightRepository
import com.example.edicalories.domain.BodyWeight
import com.example.edicalories.domain.CalorieBalance
import com.example.edicalories.domain.ChartPeriod
import com.example.edicalories.domain.JournalDocument
import com.example.edicalories.domain.JournalExportFormat
import com.example.edicalories.domain.JournalMeal
import com.example.edicalories.domain.JournalWeight
import com.example.edicalories.domain.MealSchedule
import com.example.edicalories.domain.MinutesOfDay
import com.example.edicalories.domain.ProgressRange
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val mealRepository: MealRepository,
    private val weightRepository: WeightRepository,
    private val journalRepository: JournalRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val selectedEpochDay = MutableStateFlow(LocalDate.now().toEpochDay())

    private val messagesChannel = Channel<UserMessage>(Channel.BUFFERED)
    val messages = messagesChannel.receiveAsFlow()

    private val mealsWindow = selectedEpochDay.flatMapLatest { centerEpochDay ->
        combine(
            mealRepository.observeMealsForRange(centerEpochDay - 1L, centerEpochDay + 1L),
            weightRepository.observeLatestBefore(centerEpochDay - 1L),
            weightRepository.observeLatestBefore(centerEpochDay),
            weightRepository.observeLatestBefore(centerEpochDay + 1L),
        ) { meals, weightBeforePrevious, weightBeforeCurrent, weightBeforeNext ->
            MealsWindow(
                centerEpochDay = centerEpochDay,
                meals = meals,
                weightBeforePrevious = weightBeforePrevious?.tenthsOfKg,
                weightBeforeCurrent = weightBeforeCurrent?.tenthsOfKg,
                weightBeforeNext = weightBeforeNext?.tenthsOfKg,
            )
        }
    }

    val uiState: StateFlow<TodayUiState> = combine(
        mealsWindow,
        preferencesRepository.dailyGoal,
        preferencesRepository.mealSchedule,
    ) { window, dailyGoal, mealSchedule ->
        val grouped = window.meals.groupBy { meal -> meal.epochDay }
        val todayEpochDay = LocalDate.now().toEpochDay()
        TodayUiState(
            previous = snapshotFor(
                epochDay = window.centerEpochDay - 1L,
                grouped = grouped,
                dailyGoal = dailyGoal,
                todayEpochDay = todayEpochDay,
                priorWeightTenths = window.weightBeforePrevious,
            ),
            current = snapshotFor(
                epochDay = window.centerEpochDay,
                grouped = grouped,
                dailyGoal = dailyGoal,
                todayEpochDay = todayEpochDay,
                priorWeightTenths = window.weightBeforeCurrent,
            ),
            next = snapshotFor(
                epochDay = window.centerEpochDay + 1L,
                grouped = grouped,
                dailyGoal = dailyGoal,
                todayEpochDay = todayEpochDay,
                priorWeightTenths = window.weightBeforeNext,
            ),
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

    private val progressVisible = MutableStateFlow(false)
    private val chartPeriod = MutableStateFlow(ChartPeriod.Days30)

    val selectedDayWeightTenths: StateFlow<Int?> = selectedEpochDay
        .flatMapLatest { epochDay ->
            weightRepository.observeForDay(epochDay)
                .map { entry -> entry?.tenthsOfKg }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private val earliestLoggedEpochDay = combine(
        mealRepository.observeMinEpochDay(),
        weightRepository.observeMinEpochDay(),
    ) { mealMin, weightMin ->
        listOfNotNull(mealMin, weightMin).minOrNull()
    }

    val progressUiState: StateFlow<ProgressUiState> = combine(
        progressVisible,
        chartPeriod,
        earliestLoggedEpochDay,
        preferencesRepository.dailyGoal,
    ) { visible, period, earliest, dailyGoal ->
        if (!visible) {
            null
        } else {
            val todayEpochDay = LocalDate.now().toEpochDay()
            ProgressQuery(
                period = period,
                fromEpochDay = ProgressRange.startEpochDay(period, todayEpochDay, earliest),
                toEpochDay = todayEpochDay,
                dailyGoal = dailyGoal,
            )
        }
    }.flatMapLatest { query ->
        if (query == null) {
            flowOf(ProgressUiState())
        } else {
            combine(
                mealRepository.observeDayTotals(query.fromEpochDay, query.toEpochDay),
                weightRepository.observeForRange(query.fromEpochDay, query.toEpochDay),
            ) { totals, weights ->
                ProgressUiState(
                    period = query.period,
                    fromEpochDay = query.fromEpochDay,
                    toEpochDay = query.toEpochDay,
                    dailyGoal = query.dailyGoal,
                    caloriePoints = caloriePoints(query.fromEpochDay, query.toEpochDay, totals),
                    weightPoints = weights.map { entry ->
                        ChartPoint(
                            epochDay = entry.epochDay,
                            value = BodyWeight.toKg(entry.tenthsOfKg),
                        )
                    },
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState(),
    )

    fun startObservingProgress() {
        progressVisible.value = true
    }

    fun stopObservingProgress() {
        progressVisible.value = false
    }

    fun setChartPeriod(period: ChartPeriod) {
        chartPeriod.value = period
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

    fun saveWeight(weightRaw: String, epochDay: Long) {
        val tenthsOfKg = BodyWeight.parseToTenths(weightRaw)
        if (tenthsOfKg == null) {
            emitMessage(UserMessage.InvalidWeight)
            return
        }
        viewModelScope.launch {
            runCatching {
                weightRepository.upsert(epochDay, tenthsOfKg)
            }.onSuccess {
                emitMessage(UserMessage.Saved)
            }.onFailure {
                emitMessage(UserMessage.WriteError)
            }
        }
    }

    fun clearJournal() {
        viewModelScope.launch {
            runCatching {
                journalRepository.clearLoggedEntries()
            }.onSuccess {
                emitMessage(UserMessage.JournalCleared)
            }.onFailure {
                emitMessage(UserMessage.WriteError)
            }
        }
    }

    fun exportJournal(uri: Uri, format: JournalExportFormat, resolver: ContentResolver) {
        viewModelScope.launch {
            runCatching {
                val document = buildExportDocument()
                val text = when (format) {
                    JournalExportFormat.Json -> document.toJson()
                    JournalExportFormat.Csv -> document.toCsv()
                }
                val bytes = text.toByteArray(StandardCharsets.UTF_8)
                withContext(Dispatchers.IO) {
                    val stream = resolver.openOutputStream(uri)
                        ?: error("missing output stream")
                    stream.use { output ->
                        output.write(bytes)
                        output.flush()
                    }
                }
            }.onSuccess {
                emitMessage(UserMessage.JournalExported)
            }.onFailure {
                emitMessage(UserMessage.FileWriteError)
            }
        }
    }

    fun importJournal(uri: Uri, resolver: ContentResolver) {
        viewModelScope.launch {
            val bytes = runCatching {
                withContext(Dispatchers.IO) {
                    val stream = resolver.openInputStream(uri)
                        ?: error("missing input stream")
                    stream.use { input ->
                        JournalDocument.readLimited(input)
                    }
                }
            }.getOrElse {
                emitMessage(UserMessage.FileReadError)
                return@launch
            }
            if (bytes == null) {
                emitMessage(UserMessage.InvalidImportFormat)
                return@launch
            }
            val document = JournalDocument.parseJson(bytes)
            if (document == null) {
                emitMessage(UserMessage.InvalidImportFormat)
                return@launch
            }
            runCatching {
                journalRepository.mergeImported(document.meals, document.weights)
                preferencesRepository.setDailyGoalAndSchedule(
                    document.dailyGoal,
                    MealSchedule(
                        groupingEnabled = document.groupingEnabled,
                        breakfastStart = document.breakfastStart,
                        lunchStart = document.lunchStart,
                        dinnerStart = document.dinnerStart,
                    ),
                )
            }.onSuccess {
                emitMessage(UserMessage.JournalImported)
            }.onFailure {
                emitMessage(UserMessage.WriteError)
            }
        }
    }

    private suspend fun buildExportDocument(): JournalDocument {
        val snapshot = journalRepository.snapshot()
        val goal = preferencesRepository.currentDailyGoal()
        val schedule = preferencesRepository.currentMealSchedule()
        val meals = ArrayList<JournalMeal>(snapshot.meals.size)
        for (meal in snapshot.meals) {
            meals.add(
                JournalMeal(
                    calories = meal.calories,
                    epochDay = meal.epochDay,
                    minutesOfDay = meal.minutesOfDay,
                ),
            )
        }
        val weights = ArrayList<JournalWeight>(snapshot.weights.size)
        for (weight in snapshot.weights) {
            weights.add(
                JournalWeight(
                    epochDay = weight.epochDay,
                    tenthsOfKg = weight.tenthsOfKg,
                ),
            )
        }
        return JournalDocument(
            dailyGoal = goal,
            groupingEnabled = schedule.groupingEnabled,
            breakfastStart = schedule.breakfastStart,
            lunchStart = schedule.lunchStart,
            dinnerStart = schedule.dinnerStart,
            meals = meals,
            weights = weights,
        )
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
        priorWeightTenths: Int?,
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
            priorWeightTenths = priorWeightTenths,
        )
    }

    private fun emitMessage(message: UserMessage) {
        viewModelScope.launch {
            messagesChannel.send(message)
        }
    }

    class Factory(
        private val mealRepository: MealRepository,
        private val weightRepository: WeightRepository,
        private val journalRepository: JournalRepository,
        private val preferencesRepository: PreferencesRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TodayViewModel(
                    mealRepository,
                    weightRepository,
                    journalRepository,
                    preferencesRepository,
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

private data class MealsWindow(
    val centerEpochDay: Long,
    val meals: List<Meal>,
    val weightBeforePrevious: Int?,
    val weightBeforeCurrent: Int?,
    val weightBeforeNext: Int?,
)

private data class EpochDayRange(
    val fromEpochDay: Long,
    val toEpochDay: Long,
)

private data class ProgressQuery(
    val period: ChartPeriod,
    val fromEpochDay: Long,
    val toEpochDay: Long,
    val dailyGoal: Int,
)

private fun caloriePoints(
    fromEpochDay: Long,
    toEpochDay: Long,
    totals: List<DayTotal>,
): List<ChartPoint> {
    val totalsByDay = totalsToMap(totals)
    val points = ArrayList<ChartPoint>()
    var epochDay = fromEpochDay
    while (epochDay <= toEpochDay) {
        val calories = totalsByDay[epochDay] ?: 0
        points.add(
            ChartPoint(
                epochDay = epochDay,
                value = calories.toFloat(),
            ),
        )
        epochDay += 1L
    }
    return points
}

private fun totalsToMap(totals: List<DayTotal>): Map<Long, Int> {
    val result = HashMap<Long, Int>(totals.size)
    for (total in totals) {
        result[total.epochDay] = total.totalCalories
    }
    return result
}
