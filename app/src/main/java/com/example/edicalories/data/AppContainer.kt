package com.example.edicalories.data

import android.content.Context

class AppContainer(context: Context) {
    private val database: AppDatabase = AppDatabase.getInstance(context)

    val mealRepository: MealRepository = MealRepository(database.mealDao())
    val preferencesRepository: PreferencesRepository = PreferencesRepository(context)
}
