package com.code1x5.rashod.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория для работы с фотографиями
 */
interface PhotoRepository {
    /**
     * Получение всех фотографий
     */
    fun getAllPhotos(): Flow<List<String>>
    
    /**
     * Получение фотографий для заказа
     */
    fun getPhotosByOrderId(orderId: String): Flow<List<String>>
    
    /**
     * Добавление новой фотографии
     */
    suspend fun addPhoto(filePath: String, orderId: String)
    
    /**
     * Добавление нескольких фотографий
     */
    suspend fun addPhotos(filePaths: List<String>, orderId: String)
    
    /**
     * Удаление фотографии
     */
    suspend fun deletePhoto(filePath: String, orderId: String)
    
    /**
     * Удаление фотографии по ID
     */
    suspend fun deletePhotoById(photoId: String)
    
    /**
     * Удаление всех фотографий для заказа
     */
    suspend fun deletePhotosByOrderId(orderId: String)
} 