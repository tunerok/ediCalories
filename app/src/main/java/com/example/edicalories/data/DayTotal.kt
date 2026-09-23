package com.example.edicalories.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_totals")
data class DayTotal(
    @PrimaryKey val epochDay: Long,
    val totalCalories: Int,
)
