package com.fishbuddy.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AppBlue = Color(0xFF0066CC)
val AppBlueDark = Color(0xFF004C99)
val AppBackground = Color(0xFFFFFFFF)
val AppCardBackground = Color(0xFFF8F9FA)
val SuccessGreen = Color(0xFF4CAF50)
val WarningOrange = Color(0xFFFF9800)
val DangerRed = Color(0xFFF44336)

private val LightColorScheme = lightColorScheme(
    primary = AppBlue,
    onPrimary = Color.White,
    primaryContainer = AppBlue.copy(alpha = 0.08f),
    background = AppBackground,
    surface = Color.White,
    surfaceVariant = AppCardBackground
)

@Composable
fun FishBuddyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
