package com.code1x5.rashod.ui.screens.order_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.code1x5.rashod.data.repository.ExpenseRepository
import com.code1x5.rashod.data.repository.OrderRepository
import com.code1x5.rashod.data.repository.PhotoRepository
import com.code1x5.rashod.domain.model.Expense
import com.code1x5.rashod.domain.model.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана деталей заказа
 */
@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val expenseRepository: ExpenseRepository,
    private val photoRepository: PhotoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // Получаем ID заказа из аргументов навигации
    private val orderId: String = checkNotNull(savedStateHandle["orderId"])
    
    // Состояние загрузки и ошибки
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Данные заказа
    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()
    
    // Список расходов заказа
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()
    
    // Список фотографий заказа
    private val _photos = MutableStateFlow<List<String>>(emptyList())
    val photos: StateFlow<List<String>> = _photos.asStateFlow()
    
    // События UI для навигации
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()
    
    init {
        loadOrderDetails()
    }
    
    /**
     * Загрузка деталей заказа
     */
    private fun loadOrderDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Загружаем заказ
                val order = orderRepository.getOrderById(orderId)
                if (order != null) {
                    _order.value = order
                    
                    // Загружаем расходы и фотографии
                    launch {
                        expenseRepository.getExpensesByOrderId(orderId)
                            .catch { e ->
                                _error.value = "Ошибка загрузки расходов: ${e.message}"
                            }
                            .collectLatest { expenses ->
                                _expenses.value = expenses
                            }
                    }
                    
                    launch {
                        photoRepository.getPhotosByOrderId(orderId)
                            .catch { e ->
                                _error.value = "Ошибка загрузки фотографий: ${e.message}"
                            }
                            .collectLatest { photos ->
                                _photos.value = photos
                            }
                    }
                } else {
                    _error.value = "Заказ не найден"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки заказа: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Удаление расхода
     */
    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                expenseRepository.deleteExpenseById(expenseId)
                // Обновляем заказ после удаления расхода
                loadOrderDetails()
            } catch (e: Exception) {
                _error.value = "Ошибка удаления расхода: ${e.message}"
            }
        }
    }
    
    /**
     * Удаление фотографии
     */
    fun deletePhoto(photoPath: String) {
        viewModelScope.launch {
            try {
                photoRepository.deletePhoto(photoPath, orderId)
                // Обновляем заказ после удаления фотографии
                loadOrderDetails()
            } catch (e: Exception) {
                _error.value = "Ошибка удаления фотографии: ${e.message}"
            }
        }
    }
    
    /**
     * События UI для навигации
     */
    sealed class UiEvent {
        data class NavigateToEditOrder(val orderId: String) : UiEvent()
        data class NavigateToEditExpense(val orderId: String, val expenseId: String? = null) : UiEvent()
    }
} 