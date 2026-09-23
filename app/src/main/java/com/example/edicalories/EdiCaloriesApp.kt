package com.example.edicalories

import android.app.Application
import com.example.edicalories.data.AppContainer

class EdiCaloriesApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
