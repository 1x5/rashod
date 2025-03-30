package com.code1x5.rashod.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.code1x5.rashod.data.local.entity.OrderEntity
import com.code1x5.rashod.data.local.relation.OrderWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с заказами
 */
@Dao
interface OrderDao {
    /**
     * Получение всех заказов
     */
    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>
    
    /**
     * Получение заказа по ID
     */
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): OrderEntity?
    
    /**
     * Получение заказа с деталями (расходами и фотографиями)
     */
    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderWithDetails(orderId: String): OrderWithDetails?
    
    /**
     * Получение всех заказов с деталями (расходами и фотографиями)
     */
    @Transaction
    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getAllOrdersWithDetails(): Flow<List<OrderWithDetails>>
    
    /**
     * Вставка нового заказа
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long
    
    /**
     * Обновление заказа
     */
    @Update
    suspend fun updateOrder(order: OrderEntity)
    
    /**
     * Удаление заказа
     */
    @Delete
    suspend fun deleteOrder(order: OrderEntity)
    
    /**
     * Удаление заказа по ID
     */
    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: String)
    
    /**
     * Поиск заказов по названию или клиенту
     */
    @Query("SELECT * FROM orders WHERE title LIKE '%' || :query || '%' OR client LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchOrders(query: String): Flow<List<OrderEntity>>
} 