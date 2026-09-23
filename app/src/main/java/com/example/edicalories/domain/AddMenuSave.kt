package com.example.edicalories.domain

object AddMenuSave {
    fun resolve(caloriesRaw: String, weightRaw: String): Result {
        val caloriesBlank = caloriesRaw.isBlank()
        val weightBlank = weightRaw.isBlank()
        if (caloriesBlank && weightBlank) {
            return Result.CloseEmpty
        }
        val calories = if (caloriesBlank) {
            null
        } else {
            CalorieBalance.parsePositiveCalories(caloriesRaw)
        }
        if (!caloriesBlank && calories == null) {
            return Result.InvalidCalories
        }
        val tenthsOfKg = if (weightBlank) {
            null
        } else {
            BodyWeight.parseToTenths(weightRaw)
        }
        if (!weightBlank && tenthsOfKg == null) {
            return Result.InvalidWeight
        }
        return Result.Write(
            calories = calories,
            tenthsOfKg = tenthsOfKg,
        )
    }

    sealed interface Result {
        data object CloseEmpty : Result
        data object InvalidCalories : Result
        data object InvalidWeight : Result
        data class Write(val calories: Int?, val tenthsOfKg: Int?) : Result
    }
}
