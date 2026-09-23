package com.example.edicalories

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.edicalories.data.AppContainer
import com.example.edicalories.ui.today.nightMode
import kotlinx.coroutines.runBlocking

class EdiCaloriesApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        val themeMode = runBlocking {
            container.preferencesRepository.currentThemeMode()
        }
        AppCompatDelegate.setDefaultNightMode(themeMode.nightMode())
    }
}
