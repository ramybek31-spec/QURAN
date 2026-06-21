package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Emerald400,
    secondary = Emerald500,
    tertiary = GoldBronze,
    background = ThemeBackground,
    surface = GlassSurface,
    onPrimary = ThemeBackground,
    onSecondary = Color.White,
    onTertiary = ThemeBackground,
    onBackground = Slate100,
    onSurface = Slate100,
    surfaceVariant = GlassSurfaceVariant,
    onSurfaceVariant = Slate400
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald600,
    secondary = Emerald500,
    tertiary = DeepGold,
    background = GlassLightBackground,
    surface = GlassLightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F1A15),
    onSurface = Color(0xFF0F1A15),
    surfaceVariant = Color(0xFFE2EBE5),
    onSurfaceVariant = Color(0xFF475569)
)

enum class AppThemeMode {
    LIGHT, DARK, SEPIA
}

private val SepiaColorScheme = lightColorScheme(
    primary = Color(0xFF8B5A2B), // Chocolate / warm bronze brown
    secondary = Color(0xFF5C3A21), // Deep brown
    tertiary = Color(0xFFB58900), // Warm gold
    background = Color(0xFFFBF0D9), // Creamy soft sepia background
    surface = Color(0xFFF4E7CE), // Marginally darker sepia surface
    onPrimary = Color(0xFFFBF0D9),
    onSecondary = Color(0xFFFBF0D9),
    onTertiary = Color(0xFFFBF0D9),
    onBackground = Color(0xFF433422), // Hard brown readable text
    onSurface = Color(0xFF433422),
    surfaceVariant = Color(0xFFEBDCBD),
    onSurfaceVariant = Color(0xFF705E4C)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.SEPIA -> SepiaColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
