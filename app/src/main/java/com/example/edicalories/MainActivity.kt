package com.example.edicalories

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.edicalories.ui.theme.EdiCaloriesTheme
import com.example.edicalories.ui.today.TodayScreen
import com.example.edicalories.ui.today.TodayViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as EdiCaloriesApp
        setContent {
            EdiCaloriesTheme {
                val todayViewModel: TodayViewModel = viewModel(
                    factory = TodayViewModel.Factory(
                        app.container.mealRepository,
                        app.container.weightRepository,
                        app.container.journalRepository,
                        app.container.preferencesRepository,
                    ),
                )
                TodayScreen(viewModel = todayViewModel)
            }
        }
    }
}
