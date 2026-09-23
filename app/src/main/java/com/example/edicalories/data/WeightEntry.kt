package com.example.edicalories.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weights")
data class WeightEntry(
    @PrimaryKey val epochDay: Long,
    val tenthsOfKg: Int,
)
