package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PspCyan,
    onPrimary = Color.Black,
    secondary = PspAccent,
    onSecondary = Color.Black,
    tertiary = PspGreen,
    background = PspDarkBackground,
    surface = PspSurfaceDark,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for PSP console experience
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
