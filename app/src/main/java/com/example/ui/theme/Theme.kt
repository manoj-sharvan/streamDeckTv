package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TvDarkColorScheme = darkColorScheme(
    primary = TvPrimary,
    onPrimary = Color(0xFF001E2F),
    primaryContainer = TvPrimaryVariant,
    onPrimaryContainer = Color(0xFFC7E7FF),
    secondary = TvSecondary,
    onSecondary = Color(0xFF002B73),
    secondaryContainer = Color(0xFF003F9E),
    onSecondaryContainer = Color(0xFFD8E2FF),
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
