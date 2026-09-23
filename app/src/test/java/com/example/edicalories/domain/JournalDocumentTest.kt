package com.example.edicalories.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets

class JournalDocumentTest {

    @Test
    fun parseJson_roundTripsFullDocument() {
        val original = sampleDocument()
        val parsed = JournalDocument.parseJson(original.toJson())
        assertEquals(original, parsed)
    }

    @Test
    fun parseJson_roundTripsEmptyJournal() {
        val original = emptyDocument()
        val parsed = JournalDocument.parseJson(original.toJson())
        assertEquals(original, parsed)
    }

    @Test
    fun parseJson_keepsNullMealTime() {
        val original = emptyDocument().copy(
            meals = listOf(
                JournalMeal(
                    calories = 200,
                    epochDay = 20_354L,
                    minutesOfDay = null,
                ),
            ),
        )
        val parsed = JournalDocument.parseJson(original.toJson())
        assertNotNull(parsed)
        assertEquals(1, parsed!!.meals.size)
        assertNull(parsed.meals[0].minutesOfDay)
    }

    @Test
    fun parseJson_acceptsCompactAndBom() {
        val compact = """{"format":"edicalories","version":1,"dailyGoal":1800,"groupingEnabled":false,"breakfastStart":180,"lunchStart":600,"dinnerStart":900,"meals":[],"weights":[]}"""
        assertEquals(emptyDocument(), JournalDocument.parseJson(compact))
        val withBom = "\uFEFF$compact"
        assertEquals(emptyDocument(), JournalDocument.parseJson(withBom))
        assertEquals(
            emptyDocument(),
            JournalDocument.parseJson(withBom.toByteArray(StandardCharsets.UTF_8)),
        )
    }

    @Test
    fun parseJson_rejectsCsv() {
        val csv = emptyDocument().toCsv()
        assertNull(JournalDocument.parseJson(csv))
        assertNull(JournalDocument.parseJson(csv.toByteArray(StandardCharsets.UTF_8)))
    }

    @Test
    fun parseJson_rejectsForeignJson() {
        val foreign = """{"format":"other","version":1,"dailyGoal":1800,"groupingEnabled":false,"breakfastStart":180,"lunchStart":600,"dinnerStart":900,"meals":[],"weights":[]}"""
        assertNull(JournalDocument.parseJson(foreign))
    }

    @Test
    fun parseJson_rejectsUnknownKeyAndVersion() {
        val extraKey = """{"format":"edicalories","version":1,"dailyGoal":1800,"groupingEnabled":false,"breakfastStart":180,"lunchStart":600,"dinnerStart":900,"meals":[],"weights":[],"extra":1}"""
        assertNull(JournalDocument.parseJson(extraKey))
        val nextVersion = """{"format":"edicalories","version":2,"dailyGoal":1800,"groupingEnabled":false,"breakfastStart":180,"lunchStart":600,"dinnerStart":900,"meals":[],"weights":[]}"""
        assertNull(JournalDocument.parseJson(nextVersion))
    }

    @Test
    fun parseJson_rejectsOutOfRangeValues() {
        assertNull(
            JournalDocument.parseJson(
                emptyDocument().copy(dailyGoal = 0).toJson(),
            ),
        )
        val caloriesOver = """{"format":"edicalories","version":1,"dailyGoal":1800,"groupingEnabled":false,"breakfastStart":180,"lunchStart":600,"dinnerStart":900,"meals":[{"calories":20001,"epochDay":20354,"minutesOfDay":510}],"weights":[]}"""
        assertNull(JournalDocument.parseJson(caloriesOver))
        val minutesOver = """{"format":"edicalories","version":1,"dailyGoal":1800,"groupingEnabled":false,"breakfastStart":180,"lunchStart":600,"dinnerStart":900,"meals":[{"calories":200,"epochDay":20354,"minutesOfDay":1440}],"weights":[]}"""
        assertNull(JournalDocument.parseJson(minutesOver))
        val weightOver = """{"format":"edicalories","version":1,"dailyGoal":1800,"groupingEnabled":false,"breakfastStart":180,"lunchStart":600,"dinnerStart":900,"meals":[],"weights":[{"epochDay":20354,"tenthsOfKg":4001}]}"""
        assertNull(JournalDocument.parseJson(weightOver))
        val windowsOutOfOrder = """{"format":"edicalories","version":1,"dailyGoal":1800,"groupingEnabled":true,"breakfastStart":600,"lunchStart":180,"dinnerStart":900,"meals":[],"weights":[]}"""
        assertNull(JournalDocument.parseJson(windowsOutOfOrder))
    }

    @Test
    fun parseJson_rejectsDuplicateWeightDay() {
        val duplicate = """{"format":"edicalories","version":1,"dailyGoal":1800,"groupingEnabled":false,"breakfastStart":180,"lunchStart":600,"dinnerStart":900,"meals":[],"weights":[{"epochDay":20354,"tenthsOfKg":724},{"epochDay":20354,"tenthsOfKg":730}]}"""
        assertNull(JournalDocument.parseJson(duplicate))
    }

    @Test
    fun toCsv_writesSettingsMealAndWeightRows() {
        val csv = sampleDocument().toCsv()
        assertTrue(csv.startsWith("\uFEFF"))
        val lines = csv.removePrefix("\uFEFF").trim().split('\n')
        assertEquals(
            "type,date,time,calories,weight_kg,daily_goal,meal_grouping,breakfast_start,lunch_start,dinner_start",
            lines[0],
        )
        assertEquals("settings,,,,,1800,false,03:00,10:00,15:00", lines[1])
        assertEquals("meal,2025-09-23,08:30,350,,,,,,", lines[2])
        assertEquals("weight,2025-09-23,,,72.4,,,,,", lines[3])
    }

    @Test
    fun toCsv_leavesMealTimeEmptyWhenMissing() {
        val csv = emptyDocument().copy(
            meals = listOf(
                JournalMeal(
                    calories = 200,
                    epochDay = 20_354L,
                    minutesOfDay = null,
                ),
            ),
        ).toCsv()
        val lines = csv.removePrefix("\uFEFF").trim().split('\n')
        assertEquals("meal,2025-09-23,,200,,,,,,", lines[2])
    }

    private fun sampleDocument(): JournalDocument {
        return JournalDocument(
            dailyGoal = 1800,
            groupingEnabled = false,
            breakfastStart = 180,
            lunchStart = 600,
            dinnerStart = 900,
            meals = listOf(
                JournalMeal(
                    calories = 350,
                    epochDay = 20_354L,
                    minutesOfDay = 510,
                ),
            ),
            weights = listOf(
                JournalWeight(
                    epochDay = 20_354L,
                    tenthsOfKg = 724,
                ),
            ),
        )
    }

    private fun emptyDocument(): JournalDocument {
        return JournalDocument(
            dailyGoal = 1800,
            groupingEnabled = false,
            breakfastStart = 180,
            lunchStart = 600,
            dinnerStart = 900,
            meals = emptyList(),
            weights = emptyList(),
        )
    }
}
