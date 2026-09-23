package com.example.edicalories.domain

enum class AppThemeMode(val storageValue: String) {
    System("system"),
    Light("light"),
    Dark("dark"),
    ;

    companion object {
        fun fromStorage(value: String?): AppThemeMode {
            return entries.firstOrNull { mode -> mode.storageValue == value } ?: System
        }
    }
}
