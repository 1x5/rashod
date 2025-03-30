package com.code1x5.rashod.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.code1x5.rashod.data.local.entity.ExpenseEntity
import com.code1x5.rashod.data.local.entity.OrderEntity
import com.code1x5.rashod.data.local.entity.PhotoEntity
import com.code1x5.rashod.domain.model.Order

/**
 * Класс для связи заказа с его деталями (расходы и фотографии)
 */
data class OrderWithDetails(
    @Embedded
    val order: OrderEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val expenses: List<ExpenseEntity>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val photos: List<PhotoEntity>
) {
    /**
     * Преобразование в доменную модель
     */
    fun toDomain(): Order {
        return order.toDomain(
            expenses = expenses,
            photos = photos.map { it.filePath }
        )
    }
} 