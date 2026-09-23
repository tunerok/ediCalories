package com.example.edicalories.data

import com.example.edicalories.domain.BodyWeight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeightRepository(private val weightDao: WeightDao) {
    fun observeForDay(epochDay: Long): Flow<WeightEntry?> {
        return weightDao.observeForRange(epochDay, epochDay)
            .map { entries -> entries.firstOrNull() }
    }

    fun observeForRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<WeightEntry>> {
        return weightDao.observeForRange(fromEpochDay, toEpochDay)
    }

    fun observeMinEpochDay(): Flow<Long?> {
        return weightDao.observeMinEpochDay()
    }

    fun observeLatestBefore(epochDay: Long): Flow<WeightEntry?> {
        return weightDao.observeLatestBefore(epochDay)
            .map { entries -> entries.firstOrNull() }
    }

    suspend fun upsert(epochDay: Long, tenthsOfKg: Int) {
        require(tenthsOfKg in BodyWeight.MIN_TENTHS..BodyWeight.MAX_TENTHS)
        weightDao.upsert(
            WeightEntry(
                epochDay = epochDay,
                tenthsOfKg = tenthsOfKg,
            ),
        )
    }
}
