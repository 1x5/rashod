package com.code1x5.rashod.data.local.entity

import android.util.Log
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.code1x5.rashod.domain.model.Expense
import com.code1x5.rashod.domain.model.ExpenseCategory
import java.time.LocalDate

/**
 * Сущность расхода для хранения в базе данных Room
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("orderId")] // Индекс для внешнего ключа для оптимизации запросов
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val orderId: String, // Внешний ключ для связи с заказом
    val title: String,
    val category: String, // Хранится как строка для простоты сериализации
    val amount: Long,
    val date: String, // Хранится как строка в формате ISO
    val notes: String?
) {
    /**
     * Преобразование в доменную модель
     */
    fun toDomain(): Expense {
        return Expense(
            id = id,
            title = title,
            category = ExpenseCategory.valueOf(category),
            amount = amount,
            date = LocalDate.parse(date),
            notes = notes
        )
    }
    
    companion object {
        private const val TAG = "ExpenseEntity"
        
        /**
         * Создание сущности из доменной модели
         */
        fun fromDomain(expense: Expense, orderId: String): ExpenseEntity {
            Log.i(TAG, "Создание сущности из модели: id=${expense.id}, title=${expense.title}")
            
            // Проверка обязательных полей
            require(expense.id.isNotBlank()) { "ID расхода не может быть пустым" }
            require(orderId.isNotBlank()) { "ID заказа не может быть пустым" }
            require(expense.title.isNotBlank()) { "Название расхода не может быть пустым" }
            
            // Проверка типа категории
            val categoryString = try {
                expense.category.name
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка получения названия категории", e)
                throw IllegalArgumentException("Некорректная категория расхода")
            }
            
            // Форматирование даты
            val dateString = try {
                expense.date.toString()
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка форматирования даты", e)
                LocalDate.now().toString()
            }
            
            // Проверка суммы
            if (expense.amount < 0) {
                Log.w(TAG, "Отрицательная сумма расхода: ${expense.amount}, будет использовано абсолютное значение")
            }
            
            val entity = ExpenseEntity(
                id = expense.id,
                orderId = orderId,
                title = expense.title,
                category = categoryString,
                amount = if (expense.amount < 0) Math.abs(expense.amount) else expense.amount,
                date = dateString,
                notes = expense.notes
            )
            
            Log.i(TAG, "Сущность успешно создана: id=${entity.id}, orderId=${entity.orderId}")
            return entity
        }
    }
} 