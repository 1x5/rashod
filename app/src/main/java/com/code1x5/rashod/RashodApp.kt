package com.code1x5.rashod

import android.app.Application
import android.content.Context
import android.os.Process
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Класс приложения с поддержкой Hilt для внедрения зависимостей
 */
@HiltAndroidApp
class RashodApp : Application() {

    companion object {
        private const val TAG = "RashodApp"
        private const val MAX_CRASH_LOGS = 5
    }

    override fun onCreate() {
        super.onCreate()
        
        // Установка глобального обработчика исключений
        setupExceptionHandler()
        
        Log.i(TAG, "Приложение успешно инициализировано")
    }
    
    private fun setupExceptionHandler() {
        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "Необработанное исключение: ${throwable.message}", throwable)
                
                // Сохраняем лог ошибки в файл
                saveExceptionToFile(throwable)
                
                // Очищаем старые логи
                cleanOldCrashLogs()
                
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка в обработчике исключений: ${e.message}", e)
            } finally {
                // Вызываем стандартный обработчик исключений
                defaultExceptionHandler?.uncaughtException(thread, throwable)
            }
        }
    }
    
    private fun saveExceptionToFile(throwable: Throwable) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val filename = "rashod_crash_$timestamp.txt"
            val crashDir = File(filesDir, "crash_logs")
            if (!crashDir.exists()) {
                crashDir.mkdirs()
            }
            
            val crashFile = File(crashDir, filename)
            PrintWriter(crashFile).use { writer ->
                writer.println("Дата и время: $timestamp")
                writer.println("Версия приложения: ${packageManager.getPackageInfo(packageName, 0).versionName}")
                writer.println("Устройство: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                writer.println("Android версия: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                writer.println("\nСтек-трейс ошибки:")
                
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                writer.println(sw.toString())
            }
            
            Log.i(TAG, "Лог ошибки сохранен в $filename")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при сохранении лога ошибки: ${e.message}", e)
        }
    }
    
    private fun cleanOldCrashLogs() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val crashDir = File(filesDir, "crash_logs")
                if (crashDir.exists()) {
                    val crashFiles = crashDir.listFiles() ?: return@launch
                    if (crashFiles.size > MAX_CRASH_LOGS) {
                        crashFiles.sortBy { it.lastModified() }
                        for (i in 0 until crashFiles.size - MAX_CRASH_LOGS) {
                            if (crashFiles[i].delete()) {
                                Log.i(TAG, "Удален старый лог ошибки: ${crashFiles[i].name}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при очистке старых логов: ${e.message}", e)
            }
        }
    }
} 