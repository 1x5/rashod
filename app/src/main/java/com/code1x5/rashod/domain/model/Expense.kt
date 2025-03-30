package com.code1x5.rashod.domain.model

import java.time.LocalDate
import java.util.UUID

/**
 * Модель расхода
 */
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: ExpenseCategory,
    val amount: Long, // сумма в копейках
    val date: LocalDate,
    val notes: String? = null
)

/**
 * Категории расходов
 */
enum class ExpenseCategory {
    MATERIALS, // Материалы
    TOOLS, // Инструменты
    TRANSPORT, // Транспорт
    FOOD, // Питание
    OTHER // Прочее
} 