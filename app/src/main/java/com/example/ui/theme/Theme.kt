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
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = TextPrimary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkOutline,
    surfaceContainer = DarkSurfaceVariant,
    error = Color.Red,
    errorContainer = Color(0xFF400000)
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightSurfaceContainer,
    onPrimaryContainer = LightOnBackground,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightBackground,
    onSurface = LightOnBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    surfaceContainer = LightSurfaceContainer,
    error = Color.Red,
    errorContainer = Color(0xFFFFD0D0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useAccentColors: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dynamicColor = useAccentColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && darkTheme -> dynamicDarkColorScheme(LocalContext.current).copy(
            background = DarkBackground,
            surface = DarkSurface,
            surfaceVariant = DarkSurfaceVariant,
            surfaceContainer = DarkSurfaceVariant,
            onBackground = DarkOnBackground,
            onSurface = DarkOnBackground,
            onSurfaceVariant = TextSecondary
        )
        dynamicColor && !darkTheme -> dynamicLightColorScheme(LocalContext.current).copy(
            background = LightBackground,
            surface = LightBackground,
            surfaceVariant = LightSurfaceVariant,
            surfaceContainer = LightSurfaceContainer,
            onBackground = LightOnBackground,
            onSurface = LightOnBackground,
            onSurfaceVariant = LightOnSurfaceVariant
        )
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
