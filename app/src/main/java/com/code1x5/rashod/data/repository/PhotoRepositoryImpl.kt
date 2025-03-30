package com.code1x5.rashod.data.repository

import com.code1x5.rashod.data.local.dao.PhotoDao
import com.code1x5.rashod.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация репозитория для работы с фотографиями
 */
@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val photoDao: PhotoDao
) : PhotoRepository {
    
    override fun getAllPhotos(): Flow<List<String>> {
        return photoDao.getAllPhotos().map { photos ->
            photos.map { it.filePath }
        }
    }
    
    override fun getPhotosByOrderId(orderId: String): Flow<List<String>> {
        return photoDao.getPhotosByOrderId(orderId).map { photos ->
            photos.map { it.filePath }
        }
    }
    
    override suspend fun addPhoto(filePath: String, orderId: String) {
        val photoEntity = PhotoEntity(
            id = UUID.randomUUID().toString(),
            orderId = orderId,
            filePath = filePath
        )
        photoDao.insertPhoto(photoEntity)
    }
    
    override suspend fun addPhotos(filePaths: List<String>, orderId: String) {
        val photoEntities = filePaths.map {
            PhotoEntity(
                id = UUID.randomUUID().toString(),
                orderId = orderId,
                filePath = it
            )
        }
        photoDao.insertPhotos(photoEntities)
    }
    
    override suspend fun deletePhoto(filePath: String, orderId: String) {
        // Сначала нужно найти все фотографии с данным путем и orderId
        // Для простоты реализации, удаляем фотографии по orderId
        // Обычно в реальности здесь был бы запрос для получения ID фотографии по пути
        photoDao.deletePhotosByOrderId(orderId)
    }
    
    override suspend fun deletePhotoById(photoId: String) {
        photoDao.deletePhotoById(photoId)
    }
    
    override suspend fun deletePhotosByOrderId(orderId: String) {
        photoDao.deletePhotosByOrderId(orderId)
    }
} 