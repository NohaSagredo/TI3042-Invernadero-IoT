package com.example.appinvernaderoautonomo.ui.theme

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
    primary = PastelGreenLight,
    onPrimary = PastelGreenDark,
    primaryContainer = Color(0xFF22442C),
    onPrimaryContainer = PastelGreenLight,
    secondary = PastelSkyMedium,
    onSecondary = PastelSkyDeep,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceCard,
    onBackground = Color(0xFFE2E9E0),
    onSurface = Color(0xFFE2E9E0),
    outline = Color(0xFF354E3E)
)

private val LightColorScheme = lightColorScheme(
    primary = PastelGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = PastelGreenLight,
    onPrimaryContainer = PastelGreenDark,
    secondary = PastelSkyPrimary,
    onSecondary = Color.White,
    secondaryContainer = PastelSkyLight,
    onSecondaryContainer = PastelSkyDeep,
    tertiary = PastelSunflower,
    onTertiary = PastelSunflowerDark,
    tertiaryContainer = PastelSunflowerLight,
    onTertiaryContainer = PastelSunflowerDark,
    background = PastelBackground,
    surface = PastelSurface,
    surfaceVariant = PastelGreenSoft,
    onBackground = PastelTextPrimary,
    onSurface = PastelTextPrimary,
    outline = PastelBorder
)

@Composable
fun AppInvernaderoAutonomoTheme(
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
