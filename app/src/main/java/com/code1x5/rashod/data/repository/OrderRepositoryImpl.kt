package com.code1x5.rashod.data.repository

import com.code1x5.rashod.data.local.dao.OrderDao
import com.code1x5.rashod.data.local.entity.OrderEntity
import com.code1x5.rashod.domain.model.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация репозитория для работы с заказами
 */
@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao
) : OrderRepository {
    
    override fun getAllOrders(): Flow<List<Order>> {
        return orderDao.getAllOrdersWithDetails().map { orders ->
            orders.map { it.toDomain() }
        }
    }
    
    override suspend fun getOrderById(orderId: String): Order? {
        val orderWithDetails = orderDao.getOrderWithDetails(orderId)
        return orderWithDetails?.toDomain()
    }
    
    override suspend fun addOrder(order: Order) {
        try {
            android.util.Log.i("OrderRepositoryImpl", "Converting order to entity: ${order.id}, ${order.title}")
            val orderEntity = OrderEntity.fromDomain(order)
            android.util.Log.i("OrderRepositoryImpl", "Order entity created successfully")
            
            android.util.Log.i("OrderRepositoryImpl", "Inserting order into database")
            val result = orderDao.insertOrder(orderEntity)
            android.util.Log.i("OrderRepositoryImpl", "Order inserted successfully, result: $result")
        } catch (e: Exception) {
            android.util.Log.e("OrderRepositoryImpl", "Error adding order: ${e.javaClass.simpleName} - ${e.message}", e)
            e.printStackTrace()
            throw e
        }
    }
    
    override suspend fun updateOrder(order: Order) {
        val orderEntity = OrderEntity.fromDomain(order)
        orderDao.updateOrder(orderEntity)
    }
    
    override suspend fun deleteOrder(order: Order) {
        val orderEntity = OrderEntity.fromDomain(order)
        orderDao.deleteOrder(orderEntity)
    }
    
    override suspend fun deleteOrderById(orderId: String) {
        orderDao.deleteOrderById(orderId)
    }
    
    override fun searchOrders(query: String): Flow<List<Order>> {
        return orderDao.searchOrders(query).map { orders ->
            orders.map { orderEntity ->
                // Для поиска получаем только базовую информацию без деталей
                orderEntity.toDomain()
            }
        }
    }
} 