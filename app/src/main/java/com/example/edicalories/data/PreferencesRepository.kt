package com.example.edicalories.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.edicalories.domain.AppThemeMode
import com.example.edicalories.domain.CalorieBalance
import com.example.edicalories.domain.MealSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(context: Context) {
    private val dataStore: DataStore<Preferences> = context.applicationContext.dataStore

    val dailyGoal: Flow<Int> = dataStore.data.map { preferences ->
        preferences[DAILY_GOAL_KEY] ?: CalorieBalance.DEFAULT_DAILY_GOAL
    }

    val mealSchedule: Flow<MealSchedule> = dataStore.data.map { preferences ->
        MealSchedule(
            groupingEnabled = preferences[MEAL_GROUPING_ENABLED_KEY] ?: false,
            breakfastStart = preferences[BREAKFAST_START_KEY] ?: MealSchedule.DEFAULT_BREAKFAST_START,
            lunchStart = preferences[LUNCH_START_KEY] ?: MealSchedule.DEFAULT_LUNCH_START,
            dinnerStart = preferences[DINNER_START_KEY] ?: MealSchedule.DEFAULT_DINNER_START,
        )
    }

    suspend fun currentDailyGoal(): Int {
        return dailyGoal.first()
    }

    suspend fun currentMealSchedule(): MealSchedule {
        return mealSchedule.first()
    }

    suspend fun currentThemeMode(): AppThemeMode {
        val stored = dataStore.data.first()[THEME_MODE_KEY]
        return AppThemeMode.fromStorage(stored)
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.storageValue
        }
    }

    suspend fun setDailyGoalAndSchedule(goal: Int, schedule: MealSchedule) {
        require(goal in CalorieBalance.MIN_DAILY_GOAL..CalorieBalance.MAX_DAILY_GOAL)
        if (schedule.groupingEnabled) {
            require(schedule.isValid())
        }
        dataStore.edit { preferences ->
            preferences[DAILY_GOAL_KEY] = goal
            preferences[MEAL_GROUPING_ENABLED_KEY] = schedule.groupingEnabled
            preferences[BREAKFAST_START_KEY] = schedule.breakfastStart
            preferences[LUNCH_START_KEY] = schedule.lunchStart
            preferences[DINNER_START_KEY] = schedule.dinnerStart
        }
    }

    private companion object {
        val DAILY_GOAL_KEY = intPreferencesKey("daily_goal")
        val MEAL_GROUPING_ENABLED_KEY = booleanPreferencesKey("meal_grouping_enabled")
        val BREAKFAST_START_KEY = intPreferencesKey("breakfast_start_minutes")
        val LUNCH_START_KEY = intPreferencesKey("lunch_start_minutes")
        val DINNER_START_KEY = intPreferencesKey("dinner_start_minutes")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
}
