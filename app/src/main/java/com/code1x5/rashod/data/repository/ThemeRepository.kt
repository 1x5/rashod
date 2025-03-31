package com.code1x5.rashod.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория для работы с настройками темы приложения
 */
interface ThemeRepository {
    /**
     * Получение Flow со статусом темной темы
     */
    fun isDarkTheme(): Flow<Boolean>
    
    /**
     * Включение/выключение темной темы
     */
    suspend fun setDarkTheme(isDarkTheme: Boolean)
} 