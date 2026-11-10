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
    primary = SproutGreen,
    onPrimary = Color(0xFF003912),
    primaryContainer = ForestGreenPrimary,
    onPrimaryContainer = Color(0xFFA6F5B2),
    secondary = MintAccent,
    onSecondary = Color(0xFFFFFFFF),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceCard,
    onBackground = Color(0xFFE2E9E0),
    onSurface = Color(0xFFE2E9E0)
)

private val LightColorScheme = lightColorScheme(
    primary = LeafGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF002107),
    secondary = MintAccent,
    onSecondary = Color.White,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceCard,
    onBackground = Color(0xFF191D19),
    onSurface = Color(0xFF191D19)
)

@Composable
fun AppInvernaderoAutonomoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desactivado para mantener la identidad visual del invernadero
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
