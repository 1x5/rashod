package com.code1x5.rashod.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.code1x5.rashod.ui.screens.orders.OrdersListScreen
import com.code1x5.rashod.ui.screens.order_details.OrderDetailsScreen
import com.code1x5.rashod.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.delay

private const val TAG = "RashodNavHost"

/**
 * Основной навигационный граф приложения
 */
@Composable
fun RashodNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {}
) {
    // Состояние загрузки
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Обработчик навигации с защитой от ошибок
    val safeNavigate: (String) -> Unit = { route ->
        runCatching {
            navController.navigate(route)
        }.onFailure { e ->
            Log.e(TAG, "Ошибка навигации к $route: ${e.message}", e)
            onError("Ошибка навигации: ${e.message}")
        }
    }
    
    // Безопасный возврат назад
    val safePopBack: () -> Unit = {
        runCatching {
            navController.popBackStack()
        }.onFailure { e ->
            Log.e(TAG, "Ошибка при возврате назад: ${e.message}", e)
            onError("Ошибка навигации: ${e.message}")
        }
    }

    // Эффект для инициализации с задержкой
    LaunchedEffect(key1 = true) {
        try {
            delay(500) // Небольшая задержка для стабилизации ресурсов
            isLoading = false
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при инициализации навигации: ${e.message}", e)
            hasError = true
            errorMessage = "Ошибка инициализации: ${e.message}"
            onError(errorMessage)
        }
    }

    // Показываем индикатор загрузки или экран ошибки
    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        hasError -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage)
            }
        }
        else -> {
            // Основная навигация
            NavHost(
                navController = navController,
                startDestination = Screen.OrdersList.route,
                modifier = modifier
            ) {
                // Список заказов
                composable(route = Screen.OrdersList.route) {
                    OrdersListScreen(
                        onOrderClick = { orderId ->
                            safeNavigate(Screen.OrderDetails.createRoute(orderId))
                        },
                        onAddOrderClick = {
                            safeNavigate(Screen.AddEditOrder.createRoute())
                        },
                        onSettingsClick = {
                            safeNavigate(Screen.Settings.route)
                        }
                    )
                }
                
                // Детали заказа
                composable(
                    route = Screen.OrderDetails.route,
                    arguments = listOf(
                        navArgument("orderId") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId")
                    if (orderId == null) {
                        Log.e(TAG, "orderId равен null в деталях заказа")
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Ошибка: не указан ID заказа")
                        }
                        return@composable
                    }
                    
                    OrderDetailsScreen(
                        orderId = orderId,
                        onNavigateBack = safePopBack,
                        onEditOrderClick = { orderToEditId ->
                            safeNavigate(Screen.AddEditOrder.createRoute(orderToEditId))
                        },
                        onAddExpenseClick = { orderIdForExpense ->
                            safeNavigate(Screen.AddEditExpense.createRoute(orderIdForExpense))
                        },
                        onExpenseClick = { orderIdForExpense, expenseId ->
                            safeNavigate(Screen.AddEditExpense.createRoute(orderIdForExpense, expenseId))
                        }
                    )
                }
                
                // Настройки
                composable(route = Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateBack = safePopBack
                    )
                }
                
                // Добавление/редактирование заказа
                composable(
                    route = Screen.AddEditOrder.route,
                    arguments = listOf(
                        navArgument("orderId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId")
                    com.code1x5.rashod.ui.screens.edit_order.EditOrderScreen(
                        orderId = orderId,
                        onNavigateBack = safePopBack
                    )
                }
                
                // Добавление/редактирование расхода
                composable(
                    route = Screen.AddEditExpense.route,
                    arguments = listOf(
                        navArgument("orderId") {
                            type = NavType.StringType
                        },
                        navArgument("expenseId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId")
                    if (orderId == null) {
                        Log.e(TAG, "orderId равен null при редактировании расхода")
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Ошибка: не указан ID заказа для расхода")
                        }
                        return@composable
                    }
                    
                    val expenseId = backStackEntry.arguments?.getString("expenseId")
                    com.code1x5.rashod.ui.screens.edit_expense.EditExpenseScreen(
                        orderId = orderId,
                        expenseId = expenseId,
                        onNavigateBack = safePopBack
                    )
                }
                
                // Список расходов
                composable(
                    route = Screen.ExpensesList.route,
                    arguments = listOf(
                        navArgument("orderId") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId")
                    if (orderId == null) {
                        Log.e(TAG, "orderId равен null в списке расходов")
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Ошибка: не указан ID заказа для списка расходов")
                        }
                        return@composable
                    }
                    
                    // TODO: Реализовать экран списка расходов, когда будет создан компонент
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Список расходов для заказа $orderId")
                    }
                }
                
                // TODO: Добавить остальные экраны как только будут созданы их компоненты
            }
        }
    }
} 