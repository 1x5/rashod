package com.code1x5.rashod.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.code1x5.rashod.data.local.converter.DateConverter
import com.code1x5.rashod.data.local.dao.ExpenseDao
import com.code1x5.rashod.data.local.dao.OrderDao
import com.code1x5.rashod.data.local.dao.PhotoDao
import com.code1x5.rashod.data.local.entity.ExpenseEntity
import com.code1x5.rashod.data.local.entity.OrderEntity
import com.code1x5.rashod.data.local.entity.PhotoEntity

/**
 * База данных Room для приложения
 */
@Database(
    entities = [OrderEntity::class, ExpenseEntity::class, PhotoEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class RashodDatabase : RoomDatabase() {
    
    abstract fun orderDao(): OrderDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun photoDao(): PhotoDao
    
    companion object {
        const val DATABASE_NAME = "rashod_database"
    }
} 