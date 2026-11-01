package com.movies.cinetrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CineTrackColors = darkColorScheme(
    primary = CineTrackRed,
    onPrimary = Color(0xFF3B0009),
    primaryContainer = CineTrackDarkRed,
    onPrimaryContainer = Color.White,
    secondary = CineTrackGold,
    background = CineTrackBackground,
    surface = CineTrackSurface,
    surfaceVariant = CineTrackCard,
    surfaceContainer = CineTrackSurface,
    onBackground = Color(0xFFF4F1F2),
    onSurface = Color(0xFFF4F1F2),
    onSurfaceVariant = Color(0xFFCAC6CE),
)

@Composable
fun CineTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CineTrackColors, typography = Typography, content = content)
}