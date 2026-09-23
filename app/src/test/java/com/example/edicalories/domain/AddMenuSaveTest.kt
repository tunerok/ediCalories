package com.example.edicalories.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AddMenuSaveTest {

    @Test
    fun resolve_closesWhenBothFieldsAreBlank() {
        assertEquals(
            AddMenuSave.Result.CloseEmpty,
            AddMenuSave.resolve("  ", ""),
        )
    }

    @Test
    fun resolve_writesOnlyTheFilledField() {
        assertEquals(
            AddMenuSave.Result.Write(calories = 120, tenthsOfKg = null),
            AddMenuSave.resolve("120", ""),
        )
        assertEquals(
            AddMenuSave.Result.Write(calories = null, tenthsOfKg = 805),
            AddMenuSave.resolve("", "80.5"),
        )
    }

    @Test
    fun resolve_writesBothWhenBothAreFilled() {
        assertEquals(
            AddMenuSave.Result.Write(calories = 250, tenthsOfKg = 700),
            AddMenuSave.resolve("250", "70"),
        )
    }

    @Test
    fun resolve_rejectsInvalidCaloriesBeforeWeight() {
        assertEquals(
            AddMenuSave.Result.InvalidCalories,
            AddMenuSave.resolve("0", "70"),
        )
        assertEquals(
            AddMenuSave.Result.InvalidWeight,
            AddMenuSave.resolve("100", "10"),
        )
    }
}
