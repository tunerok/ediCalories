package com.example.edicalories.data

import kotlinx.coroutines.flow.Flow

class MealRepository(private val mealDao: MealDao) {
    fun observeMealsForDay(epochDay: Long): Flow<List<Meal>> {
        return mealDao.observeForDay(epochDay)
    }

    fun observeMealsForRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<Meal>> {
        return mealDao.observeForRange(fromEpochDay, toEpochDay)
    }

    fun observeDayTotals(fromEpochDay: Long, toEpochDay: Long): Flow<List<DayTotal>> {
        return mealDao.observeDayTotals(fromEpochDay, toEpochDay)
    }

    suspend fun add(calories: Int, epochDay: Long, minutesOfDay: Int): Long {
        return mealDao.insert(
            Meal(
                calories = calories,
                epochDay = epochDay,
                minutesOfDay = minutesOfDay,
            ),
        )
    }

    suspend fun update(meal: Meal) {
        mealDao.update(meal)
    }

    suspend fun delete(meal: Meal) {
        mealDao.delete(meal)
    }
}
