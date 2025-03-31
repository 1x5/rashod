package com.code1x5.rashod

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.runtime.collectAsState
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
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val isLoading by remember { mutableStateOf(false) }
            val error by remember { mutableStateOf<String?>(null) }
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            
            RashodTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLoading) {
                        // Показываем индикатор загрузки
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (error != null) {
                        // Показываем ошибку
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Произошла ошибка: $error",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        // Основная навигация
                        val navController = rememberNavController()
                        RashodNavHost(
                            navController = navController,
                            onError = { errorMessage ->
                                Log.e(TAG, "Ошибка навигации: $errorMessage")
                            }
                        )
                    }
                }
            }
        }
    }
}