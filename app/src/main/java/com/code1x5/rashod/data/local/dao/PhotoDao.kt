package com.code1x5.rashod.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.code1x5.rashod.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с фотографиями
 */
@Dao
interface PhotoDao {
    /**
     * Получение всех фотографий
     */
    @Query("SELECT * FROM photos")
    fun getAllPhotos(): Flow<List<PhotoEntity>>
    
    /**
     * Получение фотографий по ID заказа
     */
    @Query("SELECT * FROM photos WHERE orderId = :orderId")
    fun getPhotosByOrderId(orderId: String): Flow<List<PhotoEntity>>
    
    /**
     * Вставка новой фотографии
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long
    
    /**
     * Вставка нескольких фотографий
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)
    
    /**
     * Удаление фотографии
     */
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
    
    /**
     * Удаление фотографии по ID
     */
    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: String)
    
    /**
     * Удаление всех фотографий для заказа
     */
    @Query("DELETE FROM photos WHERE orderId = :orderId")
    suspend fun deletePhotosByOrderId(orderId: String)
} 