package com.example.edicalories.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.edicalories.domain.CalorieBalance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(context: Context) {
    private val dataStore: DataStore<Preferences> = context.applicationContext.dataStore

    val dailyGoal: Flow<Int> = dataStore.data.map { preferences ->
        preferences[DAILY_GOAL_KEY] ?: CalorieBalance.DEFAULT_DAILY_GOAL
    }

    suspend fun setDailyGoal(goal: Int) {
        require(goal in CalorieBalance.MIN_DAILY_GOAL..CalorieBalance.MAX_DAILY_GOAL)
        dataStore.edit { preferences ->
            preferences[DAILY_GOAL_KEY] = goal
        }
    }

    private companion object {
        val DAILY_GOAL_KEY = intPreferencesKey("daily_goal")
    }
}
