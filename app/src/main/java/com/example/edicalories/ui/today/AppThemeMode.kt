package com.example.edicalories.ui.today

import androidx.appcompat.app.AppCompatDelegate
import com.example.edicalories.domain.AppThemeMode

fun AppThemeMode.nightMode(): Int {
    return when (this) {
        AppThemeMode.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        AppThemeMode.Light -> AppCompatDelegate.MODE_NIGHT_NO
        AppThemeMode.Dark -> AppCompatDelegate.MODE_NIGHT_YES
    }
}

fun AppThemeMode.Companion.current(): AppThemeMode {
    return when (AppCompatDelegate.getDefaultNightMode()) {
        AppCompatDelegate.MODE_NIGHT_NO -> AppThemeMode.Light
        AppCompatDelegate.MODE_NIGHT_YES -> AppThemeMode.Dark
        else -> AppThemeMode.System
    }
}

fun AppThemeMode.Companion.apply(mode: AppThemeMode) {
    val stored = AppCompatDelegate.getDefaultNightMode()
    val alreadyApplied = when (mode) {
        AppThemeMode.Light -> stored == AppCompatDelegate.MODE_NIGHT_NO
        AppThemeMode.Dark -> stored == AppCompatDelegate.MODE_NIGHT_YES
        AppThemeMode.System -> stored == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM ||
            stored == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
    }
    if (alreadyApplied) {
        return
    }
    AppCompatDelegate.setDefaultNightMode(mode.nightMode())
}
