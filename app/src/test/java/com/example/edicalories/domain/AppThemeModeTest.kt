package com.example.edicalories.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AppThemeModeTest {

    @Test
    fun fromStorage_mapsKnownValuesAndFallsBackToSystem() {
        assertEquals(AppThemeMode.System, AppThemeMode.fromStorage(null))
        assertEquals(AppThemeMode.System, AppThemeMode.fromStorage("system"))
        assertEquals(AppThemeMode.Light, AppThemeMode.fromStorage("light"))
        assertEquals(AppThemeMode.Dark, AppThemeMode.fromStorage("dark"))
        assertEquals(AppThemeMode.System, AppThemeMode.fromStorage("unknown"))
    }
}
