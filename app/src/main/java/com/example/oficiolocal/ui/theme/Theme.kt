package com.example.oficiolocal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun OficioLocalTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}