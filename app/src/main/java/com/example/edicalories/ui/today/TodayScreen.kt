package com.example.edicalories.ui.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.edicalories.R
import com.example.edicalories.data.Meal
import com.example.edicalories.domain.CalorieBalance
import com.example.edicalories.domain.MealSchedule

private sealed interface TodaySheet {
    data object None : TodaySheet
    data class Edit(val meal: Meal) : TodaySheet
    data object Settings : TodaySheet
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(viewModel: TodayViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var sheet by remember { mutableStateOf<TodaySheet>(TodaySheet.None) }
    var addMenuOpen by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            val text = when (message) {
                is UserMessage.Added -> return@collect
                UserMessage.Saved -> context.getString(R.string.saved)
                UserMessage.Deleted -> context.getString(R.string.deleted)
                UserMessage.InvalidCalories -> context.getString(R.string.invalid_calories)
                UserMessage.InvalidGoal -> context.getString(R.string.invalid_goal)
                UserMessage.InvalidMealWindows -> context.getString(R.string.invalid_meal_windows)
                UserMessage.WriteError -> context.getString(R.string.write_error)
            }
            snackbarHostState.showSnackbar(text)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TodayTopBar(
                    selectedEpochDay = state.selectedEpochDay,
                    isToday = state.isToday,
                    onPickDate = { showDatePicker = true },
                    onToday = viewModel::selectToday,
                    onSettings = { sheet = TodaySheet.Settings },
                )
            },
            floatingActionButton = {
                if (!addMenuOpen) {
                    FloatingActionButton(
                        onClick = { addMenuOpen = true },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.add_meal),
                        )
                    }
                }
            },
        ) { innerPadding ->
            DaySwipeContainer(
                enabled = !addMenuOpen && sheet == TodaySheet.None && !showDatePicker,
                selectedEpochDay = state.selectedEpochDay,
                onPreviousDay = viewModel::selectPreviousDay,
                onNextDay = viewModel::selectNextDay,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                previous = {
                    DayPage(
                        snapshot = state.previous,
                        schedule = state.mealSchedule,
                        onMealClick = { meal -> sheet = TodaySheet.Edit(meal) },
                    )
                },
                current = {
                    DayPage(
                        snapshot = state.current,
                        schedule = state.mealSchedule,
                        onMealClick = { meal -> sheet = TodaySheet.Edit(meal) },
                    )
                },
                next = {
                    DayPage(
                        snapshot = state.next,
                        schedule = state.mealSchedule,
                        onMealClick = { meal -> sheet = TodaySheet.Edit(meal) },
                    )
                },
            )
        }

        QuickAddOverlay(
            visible = addMenuOpen,
            onDismiss = { addMenuOpen = false },
            onQuickAdd = { calories ->
                viewModel.addQuick(calories)
                addMenuOpen = false
            },
            onCustomSave = { caloriesRaw ->
                viewModel.addCustom(caloriesRaw, state.selectedEpochDay)
                if (CalorieBalance.parsePositiveCalories(caloriesRaw) != null) {
                    addMenuOpen = false
                }
            },
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
    }

    if (showDatePicker) {
        val dayTotals by viewModel.calendarDayTotals.collectAsStateWithLifecycle()
        DisposableEffect(Unit) {
            onDispose {
                viewModel.stopObservingCalendar()
            }
        }
        DayCalendarDialog(
            selectedEpochDay = state.selectedEpochDay,
            dailyGoal = state.dailyGoal,
            dayTotals = dayTotals,
            onVisibleRangeChange = viewModel::observeCalendarRange,
            onDismiss = { showDatePicker = false },
            onConfirm = { epochDay ->
                viewModel.selectEpochDay(epochDay)
                showDatePicker = false
            },
        )
    }

    when (val current = sheet) {
        TodaySheet.None -> Unit
        is TodaySheet.Edit -> {
            MealEditorSheet(
                meal = current.meal,
                initialEpochDay = current.meal.epochDay,
                onDismiss = { sheet = TodaySheet.None },
                onSave = { caloriesRaw, epochDay ->
                    viewModel.updateMeal(current.meal, caloriesRaw, epochDay)
                    if (CalorieBalance.parsePositiveCalories(caloriesRaw) != null) {
                        sheet = TodaySheet.None
                    }
                },
                onDelete = { meal ->
                    viewModel.deleteMeal(meal)
                    sheet = TodaySheet.None
                },
            )
        }
        TodaySheet.Settings -> {
            SettingsSheet(
                currentGoal = state.dailyGoal,
                currentSchedule = state.mealSchedule,
                onDismiss = { sheet = TodaySheet.None },
                onSave = { goalRaw, schedule ->
                    viewModel.saveSettings(goalRaw, schedule)
                    val goalOk = CalorieBalance.parseDailyGoal(goalRaw) != null
                    val windowsOk = !schedule.groupingEnabled || schedule.isValid()
                    if (goalOk && windowsOk) {
                        sheet = TodaySheet.None
                    }
                },
            )
        }
    }
}

@Composable
private fun DayPage(
    snapshot: DaySnapshot,
    schedule: MealSchedule,
    onMealClick: (Meal) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        RemainingCard(
            state = snapshot,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        Text(
            text = stringResource(R.string.meals_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )
        MealList(
            meals = snapshot.meals,
            schedule = schedule,
            onMealClick = onMealClick,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = 88.dp,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayTopBar(
    selectedEpochDay: Long,
    isToday: Boolean,
    onPickDate: () -> Unit,
    onToday: () -> Unit,
    onSettings: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = formatEpochDay(selectedEpochDay),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.pick_date),
                    onClick = onPickDate,
                ),
            )
        },
        actions = {
            if (!isToday) {
                IconButton(onClick = onToday) {
                    Icon(
                        imageVector = Icons.Filled.Today,
                        contentDescription = stringResource(R.string.go_to_today),
                    )
                }
            }
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.settings),
                )
            }
        },
    )
}
