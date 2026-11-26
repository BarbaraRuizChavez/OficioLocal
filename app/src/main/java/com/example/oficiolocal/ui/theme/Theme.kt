package com.example.oficiolocal.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = White,
    primaryContainer = GreenLight,
    onPrimaryContainer = DarkGray,
    secondary = PinkAccent,
    onSecondary = DarkGray,
    tertiary = AmberAccent,
    onTertiary = DarkGray,
    tertiaryContainer = AmberContainer,
    secondaryContainer = PinkContainer,
    onSecondaryContainer = DarkGray,
    background = SoftBackground,
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
    tertiary = AmberAccent,
    onTertiary = DarkGray,
    tertiaryContainer = Color(0xFF5C4300),
    secondaryContainer = PinkAccent,
    onSecondaryContainer = White,
    background = Color(0xFF121212),
    onBackground = White,
    surface = Color(0xFF1E1E1E),
    onSurface = White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = White
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp)
)

@Composable
fun OficioLocalTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
