package com.example.property_listing_system.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Pure Blue & White Dark Theme (Deep Navy & Pure White)
private val DarkColorScheme = darkColorScheme(
    primary = DarkBluePrimary,
    onPrimary = PureWhite,
    primaryContainer = DarkNavyCard,
    onPrimaryContainer = PureWhite,
    secondary = DarkBlueSecondary,
    onSecondary = DarkNavyBackground,
    background = DarkNavyBackground,
    onBackground = DarkTextPrimary,
    surface = DarkNavySurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkNavyCard,
    onSurfaceVariant = DarkTextSecondary
)

// Pure Blue & White Light Theme (Royal Blue & Pure White)
private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = PureWhite,
    primaryContainer = BlueLightContainer,
    onPrimaryContainer = BluePrimaryDark,
    secondary = BlueSecondary,
    onSecondary = PureWhite,
    background = BlueSurfaceLight,
    onBackground = NavyTextPrimary,
    surface = PureWhite,
    onSurface = NavyTextPrimary,
    surfaceVariant = BlueLightContainer,
    onSurfaceVariant = NavyTextSecondary
)

@Composable
fun Property_Listing_SystemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode && view.context is Activity) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
