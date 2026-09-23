package com.example.edicalories.data

import androidx.room.withTransaction

class JournalRepository(
    private val database: AppDatabase,
    private val mealDao: MealDao,
    private val weightDao: WeightDao,
) {
    suspend fun clearLoggedEntries() {
        database.withTransaction {
            mealDao.deleteAllMeals()
            mealDao.deleteAllDayTotals()
            weightDao.deleteAll()
        }
    }
}
