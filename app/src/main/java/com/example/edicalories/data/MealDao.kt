package com.example.edicalories.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE epochDay = :epochDay ORDER BY id ASC")
    fun observeForDay(epochDay: Long): Flow<List<Meal>>

    @Insert
    suspend fun insert(meal: Meal): Long

    @Update
    suspend fun update(meal: Meal)

    @Delete
    suspend fun delete(meal: Meal)
}
