package com.example.edicalories.data

import kotlinx.coroutines.flow.Flow

class MealRepository(private val mealDao: MealDao) {
    fun observeMealsForDay(epochDay: Long): Flow<List<Meal>> {
        return mealDao.observeForDay(epochDay)
    }

    suspend fun add(calories: Int, epochDay: Long): Long {
        return mealDao.insert(Meal(calories = calories, epochDay = epochDay))
    }

    suspend fun update(meal: Meal) {
        mealDao.update(meal)
    }

    suspend fun delete(meal: Meal) {
        mealDao.delete(meal)
    }
}
