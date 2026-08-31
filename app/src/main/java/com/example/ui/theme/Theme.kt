package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = TelegramLightBlue,
    onPrimary = TelegramDarkBlue,
    primaryContainer = TelegramBubbleOutgoing,
    onPrimaryContainer = TextPrimary,
    secondary = CyberCyan,
    onSecondary = TelegramDarkBlue,
    secondaryContainer = CardHighlight,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonGreen,
    onTertiary = TelegramDarkBlue,
    background = TelegramChatBg,
    onBackground = TextPrimary,
    surface = TelegramSurface,
    onSurface = TextPrimary,
    surfaceVariant = TelegramCardBg,
    onSurfaceVariant = TextSecondary,
    outline = BorderStroke,
    error = CrimsonRed,
    onError = TextPrimary
)

private val LightColorScheme = DarkColorScheme // Defaulting to sleek dark cyberpunk / telegram theme for maximum bot immersion

@Composable
fun TelegramBotTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
