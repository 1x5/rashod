package com.code1x5.rashod.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * Конвертер для работы с LocalDate в базе данных Room
 */
class DateConverter {
    
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.toString()
    }
} 