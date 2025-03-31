package com.code1x5.rashod.ui.theme

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.code1x5.rashod.ui.screens.settings.SettingsViewModel

private val DarkColorScheme = darkColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A2236),
    onPrimaryContainer = Color(0xFFE8DDFF),
    secondary = AccentPositive,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1C2B1F),
    onSecondaryContainer = Color(0xFFA4FFB8),
    tertiary = AccentNeutral,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF2B2615),
    onTertiaryContainer = Color(0xFFFFE693),
    error = AccentNegative,
    onError = Color.White,
    errorContainer = Color(0xFF400000),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFDADADA),
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = Primary40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8DDFF),
    onPrimaryContainer = Primary10,
    secondary = AccentPositive,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCEFFD8),
    onSecondaryContainer = Color(0xFF002107),
    tertiary = AccentNeutral,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFFE693),
    onTertiaryContainer = Color(0xFF261900),
    error = AccentNegative,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = LightBackground,
    onBackground = Color.Black,
    surface = LightSurface,
    onSurface = Color.Black,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF4A4458),
    outline = LightOutline
)

/**
 * Основная тема приложения
 * 
 * @param darkTheme Использовать темную тему
 * @param dynamicColor Использовать динамическую цветовую схему (Android 12+)
 * @param content Содержимое, к которому применяется тема
 */
@Composable
fun RashodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val isDarkThemeFromSettings by settingsViewModel.isDarkTheme.collectAsState()
    
    // Используем настройку темы из DataStore или системную, если не указана
    val useDarkTheme = darkTheme || isDarkThemeFromSettings
    
    Log.d("RashodTheme", "Используется ${if (useDarkTheme) "темная" else "светлая"} тема")
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Современный подход к настройке системных UI элементов
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // Настраиваем контроллер вставок для прозрачных системных баров
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !useDarkTheme
                isAppearanceLightNavigationBars = !useDarkTheme
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}