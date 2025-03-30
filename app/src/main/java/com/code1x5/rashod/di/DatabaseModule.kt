package com.code1x5.rashod.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.code1x5.rashod.data.local.RashodDatabase
import com.code1x5.rashod.data.local.dao.ExpenseDao
import com.code1x5.rashod.data.local.dao.OrderDao
import com.code1x5.rashod.data.local.dao.PhotoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

/**
 * Модуль Dagger Hilt для предоставления зависимостей, связанных с базой данных
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    private const val TAG = "DatabaseModule"
    
    /**
     * Предоставляет экземпляр базы данных Room
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RashodDatabase {
        Log.i(TAG, "Инициализация базы данных Room")
        
        try {
            // Проверка файла базы данных, если существует и поврежден - удаляем
            val dbFile = context.getDatabasePath(RashodDatabase.DATABASE_NAME)
            if (dbFile.exists()) {
                try {
                    // Попытка открыть базу данных с помощью SQLite
                    val checkDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                        dbFile.absolutePath, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                    )
                    
                    // Используем rawQuery для проверки целостности
                    checkDb.rawQuery("PRAGMA quick_check", null).use { cursor ->
                        if (cursor.moveToFirst()) {
                            val result = cursor.getString(0)
                            Log.i(TAG, "Проверка целостности БД: $result")
                            if (result != "ok") {
                                Log.e(TAG, "База данных повреждена")
                                if (dbFile.delete()) {
                                    Log.i(TAG, "Поврежденная база данных успешно удалена")
                                }
                            }
                        }
                    }
                    
                    checkDb.close()
                    Log.i(TAG, "Существующая база данных проверена и в порядке")
                } catch (e: Exception) {
                    // База данных повреждена, удаляем её
                    Log.e(TAG, "База данных повреждена, удаляем: ${e.message}", e)
                    if (dbFile.delete()) {
                        Log.i(TAG, "Поврежденная база данных успешно удалена")
                    } else {
                        Log.e(TAG, "Не удалось удалить поврежденную базу данных")
                    }
                    
                    // Удаляем также файлы журнала и шарда
                    File(dbFile.parent, "${RashodDatabase.DATABASE_NAME}-shm").delete()
                    File(dbFile.parent, "${RashodDatabase.DATABASE_NAME}-wal").delete()
                }
            }
            
            return Room.databaseBuilder(
                context,
                RashodDatabase::class.java,
                RashodDatabase.DATABASE_NAME
            )
            .fallbackToDestructiveMigration() // Позволяет обновлять схему даже если нет миграции
            .setJournalMode(androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // Улучшение производительности
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.i(TAG, "База данных создана успешно")
                }
                
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.i(TAG, "База данных открыта успешно")
                    
                    // Используем правильный способ включения внешних ключей
                    try {
                        // Используем rawQuery вместо execSQL для PRAGMA
                        db.query("PRAGMA foreign_keys = ON").close()
                        Log.i(TAG, "PRAGMA foreign_keys установлен успешно")
                        
                        // WAL режим журналирования уже устанавливается через setJournalMode выше
                        // Для других прагма-директив используем правильный подход
                        db.query("PRAGMA synchronous = NORMAL").close()
                        Log.i(TAG, "PRAGMA synchronous установлен успешно")
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при установке PRAGMA директив: ${e.message}", e)
                    }
                }
            })
            .build()
            .also {
                Log.i(TAG, "База данных инициализирована успешно")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Критическая ошибка при инициализации базы данных: ${e.message}", e)
            
            // Попытка восстановить базу данных в крайнем случае
            try {
                Log.i(TAG, "Пытаемся создать базу данных в аварийном режиме")
                return Room.databaseBuilder(
                    context,
                    RashodDatabase::class.java,
                    RashodDatabase.DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
            } catch (e2: Exception) {
                Log.e(TAG, "Критическая ошибка в аварийном режиме: ${e2.message}", e2)
                throw e2
            }
        }
    }
    
    /**
     * Предоставляет DAO для заказов
     */
    @Provides
    @Singleton
    fun provideOrderDao(database: RashodDatabase) = database.orderDao()
    
    /**
     * Предоставляет DAO для расходов
     */
    @Provides
    @Singleton
    fun provideExpenseDao(database: RashodDatabase) = database.expenseDao()
    
    /**
     * Предоставляет DAO для фотографий
     */
    @Provides
    @Singleton
    fun providePhotoDao(database: RashodDatabase) = database.photoDao()
} 