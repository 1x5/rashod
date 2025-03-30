package com.code1x5.rashod.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.code1x5.rashod.domain.model.Order
import com.code1x5.rashod.domain.model.OrderStatus
import java.time.LocalDate

/**
 * Сущность заказа для хранения в базе данных Room
 */
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val client: String,
    val status: String, // Хранится как строка для простоты сериализации
    val amount: Long,
    val date: String, // Хранится как строка в формате ISO
    val income: Long?,
    val notes: String?
) {
    /**
     * Преобразование в доменную модель
     */
    fun toDomain(expenses: List<ExpenseEntity> = emptyList(), photos: List<String> = emptyList()): Order {
        return Order(
            id = id,
            title = title,
            client = client,
            status = OrderStatus.valueOf(status),
            amount = amount,
            date = LocalDate.parse(date),
            income = income,
            expenses = expenses.map { it.toDomain() },
            notes = notes,
            photos = photos
        )
    }
    
    companion object {
        /**
         * Создание сущности из доменной модели
         */
        fun fromDomain(order: Order): OrderEntity {
            try {
                android.util.Log.i("OrderEntity", "Converting order to entity: ${order.id}, ${order.title}")
                
                // Проверяем обязательные поля
                if (order.id.isBlank()) {
                    throw IllegalArgumentException("Order ID cannot be empty")
                }
                if (order.title.isBlank()) {
                    throw IllegalArgumentException("Order title cannot be empty")
                }
                if (order.client.isBlank()) {
                    throw IllegalArgumentException("Order client cannot be empty")
                }
                
                val dateStr = try {
                    order.date.toString()
                } catch (e: Exception) {
                    android.util.Log.e("OrderEntity", "Error converting date: ${e.message}")
                    LocalDate.now().toString()
                }
                
                val entity = OrderEntity(
                    id = order.id,
                    title = order.title,
                    client = order.client,
                    status = order.status.name,
                    amount = order.amount,
                    date = dateStr,
                    income = order.income,
                    notes = order.notes
                )
                
                android.util.Log.i("OrderEntity", "Entity created successfully: ${entity.id}, ${entity.title}")
                return entity
            } catch (e: Exception) {
                android.util.Log.e("OrderEntity", "Error in fromDomain: ${e.javaClass.simpleName} - ${e.message}", e)
                e.printStackTrace()
                throw e
            }
        }
    }
} 