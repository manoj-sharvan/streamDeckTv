package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TvDarkColorScheme = darkColorScheme(
    primary = TvPrimary,
    onPrimary = Color.White,
    primaryContainer = TvPrimaryVariant,
    onPrimaryContainer = Color(0xFFD0E6FF),
    secondary = TvSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2C264D),
    onSecondaryContainer = Color(0xFFE5DEFF),
    tertiary = TvAccent,
    background = TvBackground,
    onBackground = TvTextPrimary,
    surface = TvSurface,
    onSurface = TvTextPrimary,
    surfaceVariant = TvSurfaceVariant,
    onSurfaceVariant = TvTextSecondary,
    error = TvLiveRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Android TV applications require fixed high-contrast dark themes
    MaterialTheme(
        colorScheme = TvDarkColorScheme,
        typography = Typography,
        content = content
    )
}
