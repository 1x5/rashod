package com.code1x5.rashod.ui.screens.photo_viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.code1x5.rashod.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана просмотра фотографии
 */
@HiltViewModel
class PhotoViewerViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {
    
    private val _photoPath = MutableStateFlow<String?>(null)
    val photoPath: StateFlow<String?> = _photoPath.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()
    
    // ID фотографии и ID заказа
    private var photoId: String? = null
    private var orderId: String? = null
    
    /**
     * Загрузка данных фотографии
     */
    fun loadPhoto(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                photoId = id
                // В реальном приложении здесь бы загружалась фотография по ID
                _photoPath.value = "placeholder_path"
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки фотографии: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Удаление фотографии
     */
    fun deletePhoto() {
        if (photoId == null || _photoPath.value == null) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // В реальном приложении здесь бы удалялась фотография из репозитория
                // photoRepository.deletePhoto(_photoPath.value!!, orderId!!)
                _events.emit(UiEvent.NavigateBack)
            } catch (e: Exception) {
                _error.value = "Ошибка удаления фотографии: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * События UI
     */
    sealed class UiEvent {
        object NavigateBack : UiEvent()
    }
} 