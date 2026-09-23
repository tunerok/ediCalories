package com.example.edicalories.ui.today

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage(val languageTag: String?) {
    System(null),
    English("en"),
    Russian("ru"),
    ;

    companion object {
        fun current(): AppLanguage {
            val locales = AppCompatDelegate.getApplicationLocales()
            if (locales.isEmpty) {
                return System
            }
            val tag = locales[0]?.language
            return entries.firstOrNull { language -> language.languageTag == tag } ?: System
        }

        fun apply(language: AppLanguage) {
            if (language == current()) {
                return
            }
            val locales = if (language.languageTag == null) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(language.languageTag)
            }
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }
}
