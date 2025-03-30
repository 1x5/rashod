package com.code1x5.rashod.data.repository

import com.code1x5.rashod.domain.model.Order
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория для работы с заказами
 */
interface OrderRepository {
    /**
     * Получение всех заказов
     */
    fun getAllOrders(): Flow<List<Order>>
    
    /**
     * Получение заказа по ID
     */
    suspend fun getOrderById(orderId: String): Order?
    
    /**
     * Добавление нового заказа
     */
    suspend fun addOrder(order: Order)
    
    /**
     * Обновление заказа
     */
    suspend fun updateOrder(order: Order)
    
    /**
     * Удаление заказа
     */
    suspend fun deleteOrder(order: Order)
    
    /**
     * Удаление заказа по ID
     */
    suspend fun deleteOrderById(orderId: String)
    
    /**
     * Поиск заказов
     */
    fun searchOrders(query: String): Flow<List<Order>>
} 