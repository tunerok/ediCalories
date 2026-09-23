package com.example.edicalories.domain

import com.example.edicalories.data.Meal

enum class MealSlot {
    Breakfast,
    Lunch,
    Dinner,
}

data class MealGroup(
    val slot: MealSlot?,
    val meals: List<Meal>,
    val calories: Int,
)

data class MealSchedule(
    val groupingEnabled: Boolean = false,
    val breakfastStart: Int = DEFAULT_BREAKFAST_START,
    val lunchStart: Int = DEFAULT_LUNCH_START,
    val dinnerStart: Int = DEFAULT_DINNER_START,
) {
    fun isValid(): Boolean {
        return isMinutes(breakfastStart) &&
            isMinutes(lunchStart) &&
            isMinutes(dinnerStart) &&
            breakfastStart < lunchStart &&
            lunchStart < dinnerStart
    }

    fun slotOf(minutesOfDay: Int): MealSlot {
        val safe = minutesOfDay.coerceIn(MinutesOfDay.MIN_INCLUSIVE, MinutesOfDay.MAX_INCLUSIVE)
        return when {
            safe >= breakfastStart && safe < lunchStart -> MealSlot.Breakfast
            safe >= lunchStart && safe < dinnerStart -> MealSlot.Lunch
            else -> MealSlot.Dinner
        }
    }

    companion object {
        const val DEFAULT_BREAKFAST_START: Int = 3 * MinutesOfDay.MINUTES_PER_HOUR
        const val DEFAULT_LUNCH_START: Int = 10 * MinutesOfDay.MINUTES_PER_HOUR
        const val DEFAULT_DINNER_START: Int = 15 * MinutesOfDay.MINUTES_PER_HOUR

        val DEFAULT: MealSchedule = MealSchedule()

        fun groupMeals(meals: List<Meal>, schedule: MealSchedule): List<MealGroup> {
            val breakfast = ArrayList<Meal>()
            val lunch = ArrayList<Meal>()
            val dinnerEvening = ArrayList<Meal>()
            val dinnerNight = ArrayList<Meal>()
            val untimed = ArrayList<Meal>()

            for (meal in meals) {
                val minutes = meal.minutesOfDay
                if (minutes == null) {
                    untimed.add(meal)
                    continue
                }
                when (schedule.slotOf(minutes)) {
                    MealSlot.Breakfast -> breakfast.add(meal)
                    MealSlot.Lunch -> lunch.add(meal)
                    MealSlot.Dinner -> {
                        if (minutes >= schedule.dinnerStart) {
                            dinnerEvening.add(meal)
                        } else {
                            dinnerNight.add(meal)
                        }
                    }
                }
            }

            val groups = ArrayList<MealGroup>(4)
            appendIfNotEmpty(groups, MealSlot.Breakfast, breakfast)
            appendIfNotEmpty(groups, MealSlot.Lunch, lunch)
            appendIfNotEmpty(groups, MealSlot.Dinner, dinnerEvening + dinnerNight)
            appendIfNotEmpty(groups, null, untimed)
            return groups
        }

        private fun isMinutes(value: Int): Boolean {
            return value in MinutesOfDay.MIN_INCLUSIVE..MinutesOfDay.MAX_INCLUSIVE
        }

        private fun appendIfNotEmpty(
            groups: MutableList<MealGroup>,
            slot: MealSlot?,
            meals: List<Meal>,
        ) {
            if (meals.isEmpty()) {
                return
            }
            groups.add(
                MealGroup(
                    slot = slot,
                    meals = meals,
                    calories = CalorieBalance.consumed(meals.map { meal -> meal.calories }),
                ),
            )
        }
    }
}
