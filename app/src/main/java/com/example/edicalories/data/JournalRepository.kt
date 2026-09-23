package com.example.edicalories.data

import androidx.room.withTransaction
import com.example.edicalories.domain.JournalMeal
import com.example.edicalories.domain.JournalWeight

class JournalRepository(
    private val database: AppDatabase,
    private val mealDao: MealDao,
    private val weightDao: WeightDao,
) {
    suspend fun snapshot(): JournalSnapshot {
        return JournalSnapshot(
            meals = mealDao.getAllMeals(),
            weights = weightDao.getAll(),
        )
    }

    suspend fun mergeImported(
        meals: List<JournalMeal>,
        weights: List<JournalWeight>,
    ) {
        database.withTransaction {
            for (meal in meals) {
                mealDao.insert(
                    Meal(
                        calories = meal.calories,
                        epochDay = meal.epochDay,
                        minutesOfDay = meal.minutesOfDay,
                    ),
                )
            }
            for (weight in weights) {
                weightDao.upsert(
                    WeightEntry(
                        epochDay = weight.epochDay,
                        tenthsOfKg = weight.tenthsOfKg,
                    ),
                )
            }
        }
    }

    suspend fun clearLoggedEntries() {
        database.withTransaction {
            mealDao.deleteAllMeals()
            mealDao.deleteAllDayTotals()
            weightDao.deleteAll()
        }
    }
}

data class JournalSnapshot(
    val meals: List<Meal>,
    val weights: List<WeightEntry>,
)
