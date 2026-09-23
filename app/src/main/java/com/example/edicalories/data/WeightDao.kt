package com.example.edicalories.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weights WHERE epochDay = :epochDay")
    fun observeForDay(epochDay: Long): Flow<WeightEntry?>

    @Query(
        "SELECT * FROM weights WHERE epochDay BETWEEN :fromEpochDay AND :toEpochDay " +
            "ORDER BY epochDay ASC",
    )
    fun observeForRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<WeightEntry>>

    @Query("SELECT MIN(epochDay) FROM weights")
    fun observeMinEpochDay(): Flow<Long?>

    @Query(
        "SELECT * FROM weights WHERE epochDay < :epochDay " +
            "ORDER BY epochDay DESC LIMIT 1",
    )
    fun observeLatestBefore(epochDay: Long): Flow<List<WeightEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WeightEntry)

    @Query("DELETE FROM weights")
    suspend fun deleteAll()

    @Query("SELECT * FROM weights ORDER BY epochDay ASC")
    suspend fun getAll(): List<WeightEntry>
}
