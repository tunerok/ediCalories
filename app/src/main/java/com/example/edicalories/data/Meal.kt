package com.example.edicalories.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val calories: Int,
    val epochDay: Long,
    val minutesOfDay: Int? = null,
)
