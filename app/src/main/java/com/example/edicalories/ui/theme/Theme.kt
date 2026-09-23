package com.example.edicalories.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LeafGreenLight,
    onPrimary = Color(0xFF00391F),
    secondary = WarmOrangeLight,
    onSecondary = Color(0xFF4A2300),
    tertiary = Color(0xFFE8C56B),
    background = CreamDark,
    surface = SurfaceWarmDark,
    surfaceVariant = Color(0xFF3A3834),
    error = OverRedLight,
    onError = Color(0xFF690005),
)

private val LightColorScheme = lightColorScheme(
    primary = LeafGreen,
    onPrimary = Color.White,
    secondary = WarmOrange,
    onSecondary = Color.White,
    tertiary = Color(0xFF8B6914),
    background = Cream,
    surface = SurfaceWarm,
    surfaceVariant = Color(0xFFE8E2D9),
    error = OverRed,
    onError = Color.White,
)

@Composable
fun EdiCaloriesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
