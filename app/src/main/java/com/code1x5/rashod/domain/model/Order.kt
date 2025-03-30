package com.code1x5.rashod.domain.model

import java.time.LocalDate
import java.util.UUID

/**
 * Модель заказа, отображаемая на главном экране
 */
data class Order(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val client: String,
    val status: OrderStatus,
    val amount: Long, // сумма заказа в копейках
    val date: LocalDate,
    val income: Long? = null, // доход в копейках
    val expenses: List<Expense> = emptyList(), // список расходов
    val notes: String? = null,
    val photos: List<String> = emptyList() // пути к файлам фотографий
) {
    val totalExpenses: Long
        get() = expenses.sumOf { it.amount }
    
    val profit: Long
        get() = income?.minus(totalExpenses) ?: 0L
    
    val profitPercent: Double
        get() = if (income != null && income > 0L) {
            (profit.toDouble() / income.toDouble()) * 100.0
        } else 0.0
    
    val hasCompleteData: Boolean
        get() = income != null && income > 0L
}

/**
 * Статус заказа
 */
enum class OrderStatus {
    PLANNED, // Планируемый
    ACTIVE, // Активный
    COMPLETED // Завершенный
} 