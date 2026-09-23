package com.example.edicalories.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.edicalories.R
import com.example.edicalories.data.Meal
import com.example.edicalories.domain.CalorieBalance

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
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            val text = when (message) {
                is UserMessage.Added -> return@collect
                UserMessage.Saved -> context.getString(R.string.saved)
                UserMessage.Deleted -> context.getString(R.string.deleted)
                UserMessage.InvalidCalories -> context.getString(R.string.invalid_calories)
                UserMessage.InvalidGoal -> context.getString(R.string.invalid_goal)
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
                    onPreviousDay = viewModel::selectPreviousDay,
                    onNextDay = viewModel::selectNextDay,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                RemainingCard(
                    state = state,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
                Text(
                    text = stringResource(R.string.meals_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )
                MealList(
                    meals = state.meals,
                    onMealClick = { meal -> sheet = TodaySheet.Edit(meal) },
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
                onDismiss = { sheet = TodaySheet.None },
                onSave = { goalRaw ->
                    viewModel.setDailyGoal(goalRaw)
                    if (CalorieBalance.parseDailyGoal(goalRaw) != null) {
                        sheet = TodaySheet.None
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayTopBar(
    selectedEpochDay: Long,
    isToday: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onSettings: () -> Unit,
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(onClick = onPreviousDay) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = stringResource(R.string.previous_day),
                    )
                }
                Text(
                    text = formatEpochDay(selectedEpochDay),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                )
                IconButton(onClick = onNextDay) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = stringResource(R.string.next_day),
                    )
                }
            }
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
