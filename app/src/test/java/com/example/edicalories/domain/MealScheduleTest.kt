package com.example.edicalories.domain

import com.example.edicalories.data.Meal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MealScheduleTest {

    private val schedule = MealSchedule(
        groupingEnabled = true,
        breakfastStart = MealSchedule.DEFAULT_BREAKFAST_START,
        lunchStart = MealSchedule.DEFAULT_LUNCH_START,
        dinnerStart = MealSchedule.DEFAULT_DINNER_START,
    )

    @Test
    fun default_isDisabledWithMorningLunchAndAfternoonWindows() {
        assertFalse(MealSchedule.DEFAULT.groupingEnabled)
        assertEquals(180, MealSchedule.DEFAULT.breakfastStart)
        assertEquals(600, MealSchedule.DEFAULT.lunchStart)
        assertEquals(900, MealSchedule.DEFAULT.dinnerStart)
        assertTrue(MealSchedule.DEFAULT.isValid())
    }

    @Test
    fun isValid_rejectsOutOfRangeAndUnorderedWindows() {
        assertFalse(schedule.copy(breakfastStart = -1).isValid())
        assertFalse(schedule.copy(dinnerStart = MinutesOfDay.MAX_INCLUSIVE + 1).isValid())
        assertFalse(schedule.copy(breakfastStart = 180, lunchStart = 180, dinnerStart = 900).isValid())
        assertFalse(schedule.copy(breakfastStart = 600, lunchStart = 180, dinnerStart = 900).isValid())
        assertTrue(schedule.isValid())
    }

    @Test
    fun slotOf_usesHalfOpenWindowsAndWrapsDinner() {
        assertEquals(MealSlot.Dinner, schedule.slotOf(179))
        assertEquals(MealSlot.Breakfast, schedule.slotOf(180))
        assertEquals(MealSlot.Breakfast, schedule.slotOf(599))
        assertEquals(MealSlot.Lunch, schedule.slotOf(600))
        assertEquals(MealSlot.Lunch, schedule.slotOf(899))
        assertEquals(MealSlot.Dinner, schedule.slotOf(900))
        assertEquals(MealSlot.Dinner, schedule.slotOf(MinutesOfDay.MAX_INCLUSIVE))
        assertEquals(MealSlot.Dinner, schedule.slotOf(0))
    }

    @Test
    fun groupMeals_skipsEmptySlotsAndPutsUntimedLast() {
        val lunch = meal(id = 1L, calories = 400, minutesOfDay = 720)
        val untimed = meal(id = 2L, calories = 50, minutesOfDay = null)
        val groups = MealSchedule.groupMeals(listOf(untimed, lunch), schedule)

        assertEquals(2, groups.size)
        assertEquals(MealSlot.Lunch, groups[0].slot)
        assertEquals(listOf(lunch), groups[0].meals)
        assertEquals(400, groups[0].calories)
        assertEquals(null, groups[1].slot)
        assertEquals(listOf(untimed), groups[1].meals)
    }

    @Test
    fun groupMeals_ordersDinnerEveningBeforeNight() {
        val night = meal(id = 1L, calories = 100, minutesOfDay = 60)
        val breakfast = meal(id = 2L, calories = 200, minutesOfDay = 240)
        val lunch = meal(id = 3L, calories = 300, minutesOfDay = 720)
        val evening = meal(id = 4L, calories = 400, minutesOfDay = 1080)
        val groups = MealSchedule.groupMeals(
            listOf(night, breakfast, lunch, evening),
            schedule,
        )

        assertEquals(3, groups.size)
        assertEquals(listOf(breakfast), groups[0].meals)
        assertEquals(listOf(lunch), groups[1].meals)
        assertEquals(MealSlot.Dinner, groups[2].slot)
        assertEquals(listOf(evening, night), groups[2].meals)
        assertEquals(500, groups[2].calories)
    }

    private fun meal(id: Long, calories: Int, minutesOfDay: Int?): Meal {
        return Meal(
            id = id,
            calories = calories,
            epochDay = 0L,
            minutesOfDay = minutesOfDay,
        )
    }
}
