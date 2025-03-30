package com.code1x5.rashod.ui.navigation

/**
 * Пути для навигации в приложении
 */
sealed class Screen(val route: String) {
    
    // Основные экраны
    object OrdersList : Screen("orders_list")
    object OrderDetails : Screen("order_details/{orderId}") {
        fun createRoute(orderId: String) = "order_details/$orderId"
    }
    object AddEditOrder : Screen("add_edit_order?orderId={orderId}") {
        fun createRoute(orderId: String? = null) = 
            if (orderId != null) "add_edit_order?orderId=$orderId" 
            else "add_edit_order"
    }
    
    // Экраны управления расходами
    object ExpensesList : Screen("expenses_list/{orderId}") {
        fun createRoute(orderId: String) = "expenses_list/$orderId"
    }
    object AddEditExpense : Screen("add_edit_expense/{orderId}?expenseId={expenseId}") {
        fun createRoute(orderId: String, expenseId: String? = null) = 
            if (expenseId != null) "add_edit_expense/$orderId?expenseId=$expenseId" 
            else "add_edit_expense/$orderId"
    }
    
    // Настройки и авторизация
    object Settings : Screen("settings")
    object Login : Screen("login")
} 