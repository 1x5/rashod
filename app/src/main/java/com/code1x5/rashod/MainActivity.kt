package com.code1x5.rashod

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.code1x5.rashod.ui.navigation.RashodNavHost
import com.code1x5.rashod.ui.theme.RashodTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

private const val TAG = "MainActivity"

/**
 * Главное активити приложения
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Заворачиваем только вызов setContent, но не внутреннюю логику
        runCatching {
            Log.i(TAG, "Инициализация MainActivity")
            setContent {
                SafeAppContent()
            }
        }.onFailure { e ->
            Log.e(TAG, "Критическая ошибка при инициализации MainActivity: ${e.message}", e)
            // Создаем упрощенный контент при ошибке
            setContent {
                ErrorScreen("Произошла ошибка при запуске приложения")
            }
        }
    }
    
    @Composable
    private fun SafeAppContent() {
        var isReady by remember { mutableStateOf(false) }
        var hasInitError by remember { mutableStateOf(false) }
        
        // Задержка для стабилизации
        LaunchedEffect(key1 = true) {
            try {
                delay(300)
                isReady = true
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при инициализации: ${e.message}", e)
                hasInitError = true
            }
        }
        
        when {
            !isReady && !hasInitError -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            hasInitError -> {
                ErrorScreen("Не удалось запустить приложение")
            }
            else -> {
                MainContent()
            }
        }
    }
    
    @Composable
    private fun MainContent() {
        // Используем отслеживаемое состояние для обработки ошибок
        var error by remember { mutableStateOf<String?>(null) }
        
        // Реагируем на ошибки во время выполнения
        if (error != null) {
            ErrorScreen(error!!)
            return
        }
        
        RashodTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val navController = rememberNavController()
                
                Scaffold { paddingValues ->
                    RashodNavHost(
                        navController = navController,
                        modifier = Modifier.padding(paddingValues),
                        onError = { errorMessage ->
                            error = errorMessage
                            Log.e(TAG, "Ошибка в навигационном хосте: $errorMessage")
                        }
                    )
                }
            }
        }
    }
    
    @Composable
    private fun ErrorScreen(message: String) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}