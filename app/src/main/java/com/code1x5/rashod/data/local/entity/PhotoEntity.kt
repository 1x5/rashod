package com.code1x5.rashod.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность фотографии для хранения в базе данных Room
 */
@Entity(
    tableName = "photos",
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
data class PhotoEntity(
    @PrimaryKey
    val id: String,
    val orderId: String, // Внешний ключ для связи с заказом
    val filePath: String // Путь к файлу фотографии на устройстве
) 