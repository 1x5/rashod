package com.code1x5.rashod.ui.screens.add_photo

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
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel для экрана добавления фотографии
 */
@HiltViewModel
class AddPhotoViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _selectedImageUri = MutableStateFlow<String?>(null)
    val selectedImageUri: StateFlow<String?> = _selectedImageUri.asStateFlow()
    
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()
    
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()
    
    // ID заказа, к которому прикрепляется фотография
    private var orderId: String? = null
    
    /**
     * Установка ID заказа
     */
    fun setOrderId(id: String) {
        orderId = id
    }
    
    /**
     * Выбор изображения из галереи
     */
    fun selectImage() {
        // В реальном приложении здесь бы открывался диалог выбора изображения из галереи
        // и после выбора устанавливался URI выбранного изображения
        _selectedImageUri.value = "placeholder_uri"
    }
    
    /**
     * Создание новой фотографии с камеры
     */
    fun takePhoto() {
        // В реальном приложении здесь бы открывалась камера
        // и после фотографирования устанавливался URI сделанной фотографии
        _selectedImageUri.value = "placeholder_uri"
    }
    
    /**
     * Обновление описания фотографии
     */
    fun updateDescription(newDescription: String) {
        _description.value = newDescription
    }
    
    /**
     * Сохранение фотографии в репозиторий
     */
    fun savePhoto() {
        if (orderId == null || _selectedImageUri.value == null) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // В реальном приложении здесь бы копировалась фотография в хранилище приложения
                // и сохранялись данные о ней в репозиторий
                val photoPath = "placeholder_path"
                photoRepository.addPhoto(photoPath, orderId!!)
                
                _events.emit(UiEvent.NavigateBack)
            } catch (e: Exception) {
                _error.value = "Ошибка сохранения фотографии: ${e.message}"
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