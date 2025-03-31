package com.code1x5.rashod.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Создаем экземпляр DataStore на уровне объекта Context
private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

/**
 * Реализация репозитория настроек темы с использованием DataStore
 */
@Singleton
class ThemeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ThemeRepository {
    
    companion object {
        // Ключ для хранения состояния темной темы
        private val IS_DARK_THEME_KEY = booleanPreferencesKey("is_dark_theme")
    }
    
    /**
     * Получение Flow со статусом темной темы
     */
    override fun isDarkTheme(): Flow<Boolean> {
        return context.themeDataStore.data.map { preferences ->
            // По умолчанию используем системную тему (false)
            preferences[IS_DARK_THEME_KEY] ?: false
        }
    }
    
    /**
     * Включение/выключение темной темы
     */
    override suspend fun setDarkTheme(isDarkTheme: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[IS_DARK_THEME_KEY] = isDarkTheme
        }
    }
} 