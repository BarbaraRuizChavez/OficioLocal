package com.example.oficiolocal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = White,
    primaryContainer = GreenLight,
    onPrimaryContainer = DarkGray,
    secondary = PinkAccent,
    onSecondary = DarkGray,
    secondaryContainer = PinkContainer,
    onSecondaryContainer = DarkGray,
    background = White,
    onBackground = DarkGray,
    surface = White,
    onSurface = DarkGray,
    surfaceVariant = GreenLight,
    onSurfaceVariant = DarkGray
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenLight,
    onPrimary = DarkGray,
    primaryContainer = GreenPrimary,
    onPrimaryContainer = White,
    secondary = PinkContainer,
    onSecondary = DarkGray,
    secondaryContainer = PinkAccent,
    onSecondaryContainer = White,
    background = Color(0xFF121212),
    onBackground = White,
    surface = Color(0xFF1E1E1E),
    onSurface = White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = White
)

@Composable
fun OficioLocalTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
