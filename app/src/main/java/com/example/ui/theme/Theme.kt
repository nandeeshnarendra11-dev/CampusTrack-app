package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SleekPrimaryLight,
    onPrimary = SleekOnPrimaryContainer,
    primaryContainer = Color(0xFF004A98),
    onPrimaryContainer = SleekPrimaryContainer,
    secondary = Color(0xFF6DD5A4),
    onSecondary = Color(0xFF003824),
    secondaryContainer = Color(0xFF005237),
    onSecondaryContainer = SleekSecondaryContainer,
    tertiary = Color(0xFFFFB951),
    background = SleekBgDark,
    surface = SleekSurfaceDark,
    onBackground = SleekOnBgDark,
    onSurface = SleekOnSurfaceDark,
    surfaceVariant = Color(0xFF1E2B3E),
    onSurfaceVariant = Color(0xFFA0B2C6),
    outline = SleekBorderDark,
    outlineVariant = Color(0xFF202C3D)
)

private val LightColorScheme = lightColorScheme(
    primary = SleekPrimary,
    onPrimary = Color.White,
    primaryContainer = SleekPrimaryContainer,
    onPrimaryContainer = SleekOnPrimaryContainer,
    secondary = SleekSecondary,
    onSecondary = Color.White,
    secondaryContainer = SleekSecondaryContainer,
    onSecondaryContainer = SleekOnSecondaryContainer,
    tertiary = SleekTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SleekTertiaryContainer,
    error = SleekError,
    errorContainer = SleekErrorContainer,
    background = SleekBgLight,
    surface = SleekSurfaceLight,
    onBackground = SleekOnBgLight,
    onSurface = SleekOnSurfaceLight,
    surfaceVariant = SleekSurfaceVariant,
    onSurfaceVariant = SleekTextSecondaryLight,
    outline = SleekBorderLight,
    outlineVariant = Color(0xFFECEEF2)
)

@Composable
fun CampusTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep alias for backwards compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CampusTrackTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
