package com.code1x5.rashod.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DarkGreen,
    onPrimary = Color.White,
    secondary = Green,
    onSecondary = Color.White,
    tertiary = Blue,
    background = Background,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    error = Red,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = DarkGreen,
    onSecondary = Color.White,
    tertiary = Blue,
    background = DarkGray,
    onBackground = Color.White,
    surface = Color(0xFF1D1D1D),
    onSurface = Color.White,
    error = Red,
    onError = Color.White
)

@Composable
fun RashodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Современный подход с подавлением предупреждения
            // Цвет статус-бара всё еще можно устанавливать, но метод отмечен как устаревший
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            
            // Настраиваем появление статус-бара
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}