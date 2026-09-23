package com.example.edicalories.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MealDao {
    @Query(
        "SELECT * FROM meals WHERE epochDay = :epochDay " +
            "ORDER BY minutesOfDay ASC, id ASC",
    )
    abstract fun observeForDay(epochDay: Long): Flow<List<Meal>>

    @Query(
        "SELECT * FROM meals WHERE epochDay BETWEEN :fromEpochDay AND :toEpochDay " +
            "ORDER BY epochDay ASC, minutesOfDay ASC, id ASC",
    )
    abstract fun observeForRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<Meal>>

    @Query(
        "SELECT * FROM day_totals WHERE epochDay BETWEEN :fromEpochDay AND :toEpochDay",
    )
    abstract fun observeDayTotals(fromEpochDay: Long, toEpochDay: Long): Flow<List<DayTotal>>

    @Query("SELECT * FROM meals WHERE id = :id")
    abstract suspend fun getMealById(id: Long): Meal?

    @Insert
    abstract suspend fun insertMeal(meal: Meal): Long

    @Update
    abstract suspend fun updateMeal(meal: Meal)

    @Delete
    abstract suspend fun deleteMeal(meal: Meal)

    @Query(
        "UPDATE day_totals SET totalCalories = totalCalories + :delta WHERE epochDay = :epochDay",
    )
    abstract suspend fun incrementDayTotal(epochDay: Long, delta: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertDayTotal(total: DayTotal): Long

    @Query("DELETE FROM day_totals WHERE epochDay = :epochDay AND totalCalories <= 0")
    abstract suspend fun deleteNonPositiveDayTotal(epochDay: Long)

    @Query("SELECT MIN(epochDay) FROM meals")
    abstract fun observeMinEpochDay(): Flow<Long?>

    @Query(
        "SELECT * FROM meals ORDER BY epochDay ASC, minutesOfDay ASC, id ASC",
    )
    abstract suspend fun getAllMeals(): List<Meal>

    @Query("DELETE FROM meals")
    abstract suspend fun deleteAllMeals()

    @Query("DELETE FROM day_totals")
    abstract suspend fun deleteAllDayTotals()

    @Transaction
    open suspend fun insert(meal: Meal): Long {
        val id = insertMeal(meal)
        applyDelta(meal.epochDay, meal.calories)
        return id
    }

    @Transaction
    open suspend fun update(meal: Meal) {
        val existing = getMealById(meal.id) ?: return
        updateMeal(meal)
        if (existing.epochDay == meal.epochDay) {
            applyDelta(existing.epochDay, meal.calories - existing.calories)
        } else {
            applyDelta(existing.epochDay, -existing.calories)
            applyDelta(meal.epochDay, meal.calories)
        }
    }

    @Transaction
    open suspend fun delete(meal: Meal) {
        val existing = getMealById(meal.id) ?: return
        deleteMeal(existing)
        applyDelta(existing.epochDay, -existing.calories)
    }

    private suspend fun applyDelta(epochDay: Long, delta: Int) {
        if (delta == 0) {
            return
        }
        val updatedRows = incrementDayTotal(epochDay, delta)
        if (updatedRows == 0 && delta > 0) {
            insertDayTotal(DayTotal(epochDay = epochDay, totalCalories = delta))
        }
        deleteNonPositiveDayTotal(epochDay)
    }
}
