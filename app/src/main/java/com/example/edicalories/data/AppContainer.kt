package com.example.edicalories.data

import android.content.Context

class AppContainer(context: Context) {
    private val database: AppDatabase = AppDatabase.getInstance(context)
    private val mealDao = database.mealDao()
    private val weightDao = database.weightDao()

    val mealRepository: MealRepository = MealRepository(mealDao)
    val weightRepository: WeightRepository = WeightRepository(weightDao)
    val journalRepository: JournalRepository = JournalRepository(
        database = database,
        mealDao = mealDao,
        weightDao = weightDao,
    )
    val preferencesRepository: PreferencesRepository = PreferencesRepository(context)
}
