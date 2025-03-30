package com.code1x5.rashod.ui.screens.edit_order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.code1x5.rashod.data.repository.OrderRepository
import com.code1x5.rashod.domain.model.Order
import com.code1x5.rashod.domain.model.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel для экрана добавления/редактирования заказа
 */
@HiltViewModel
class EditOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(OrderState())
    val state: StateFlow<OrderState> = _state.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()
    
    // ID редактируемого заказа
    private var orderId: String? = null
    
    /**
     * Загрузка данных заказа для редактирования
     */
    fun loadOrder(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val order = orderRepository.getOrderById(id)
                if (order != null) {
                    orderId = order.id
                    _state.value = OrderState(
                        title = order.title,
                        client = order.client,
                        amount = (order.amount / 100.0).toString(),
                        income = order.income?.let { (it / 100.0).toString() } ?: "",
                        notes = order.notes ?: "",
                        status = order.status,
                        date = order.date
                    )
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
     * Обновление полей заказа
     */
    fun updateTitle(title: String) {
        _state.value = _state.value.copy(
            title = title,
            titleError = if (title.isBlank()) "Введите название заказа" else null
        )
    }
    
    fun updateClient(client: String) {
        _state.value = _state.value.copy(
            client = client,
            clientError = if (client.isBlank()) "Введите название клиента" else null
        )
    }
    
    fun updateAmount(amount: String) {
        val amountError = when {
            amount.isBlank() -> "Введите сумму заказа"
            amount.toDoubleOrNull() == null -> "Введите корректное число"
            amount.toDoubleOrNull()!! <= 0 -> "Сумма должна быть больше 0"
            else -> null
        }
        
        _state.value = _state.value.copy(
            amount = amount,
            amountError = amountError
        )
    }
    
    fun updateIncome(income: String) {
        val incomeError = when {
            income.isNotBlank() && income.toDoubleOrNull() == null -> "Введите корректное число"
            income.isNotBlank() && income.toDoubleOrNull()!! < 0 -> "Сумма не может быть отрицательной"
            else -> null
        }
        
        _state.value = _state.value.copy(
            income = income,
            incomeError = incomeError
        )
    }
    
    fun updateNotes(notes: String) {
        _state.value = _state.value.copy(notes = notes)
    }
    
    fun updateStatus(status: OrderStatus) {
        _state.value = _state.value.copy(status = status)
    }
    
    fun updateDate(date: LocalDate) {
        _state.value = _state.value.copy(date = date)
    }
    
    /**
     * Создание нового заказа
     */
    fun createOrder() {
        if (!validateInputs()) {
            android.util.Log.e("EditOrderViewModel", "Validation failed")
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                android.util.Log.i("EditOrderViewModel", "Starting order creation process")
                
                // Упрощаем логику - используем минимальные данные для заказа
                val newOrder = Order(
                    id = UUID.randomUUID().toString(),
                    title = _state.value.title,
                    client = _state.value.client,
                    status = OrderStatus.PLANNED,
                    amount = 100L, // Фиксированное значение для отладки
                    date = LocalDate.now()
                )
                
                android.util.Log.i("EditOrderViewModel", "Created order object: ${newOrder.id}, ${newOrder.title}")
                
                try {
                    android.util.Log.i("EditOrderViewModel", "Attempting to save order to repository")
                    orderRepository.addOrder(newOrder)
                    android.util.Log.i("EditOrderViewModel", "Order saved successfully")
                    
                    _events.emit(UiEvent.NavigateBack)
                    android.util.Log.i("EditOrderViewModel", "Emitted NavigateBack event")
                } catch (e: Exception) {
                    android.util.Log.e("EditOrderViewModel", "Repository error: ${e.javaClass.simpleName} - ${e.message}", e)
                    _error.value = "Ошибка сохранения: ${e.javaClass.simpleName}"
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                android.util.Log.e("EditOrderViewModel", "General error: ${e.javaClass.simpleName} - ${e.message}", e)
                _error.value = "Общая ошибка: ${e.javaClass.simpleName}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                android.util.Log.i("EditOrderViewModel", "Create order process completed")
            }
        }
    }
    
    /**
     * Обновление существующего заказа
     */
    fun updateOrder() {
        if (!validateInputs() || orderId == null) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Преобразуем значения в копейки для хранения в БД
                val amountInCents = (_state.value.amount.toDoubleOrNull() ?: 0.0) * 100
                val incomeInCents = _state.value.income.takeIf { it.isNotBlank() }
                    ?.toDoubleOrNull()?.times(100)?.toLong()
                
                val updatedOrder = Order(
                    id = orderId!!,
                    title = _state.value.title,
                    client = _state.value.client,
                    amount = amountInCents.toLong(),
                    income = incomeInCents,
                    notes = _state.value.notes.takeIf { it.isNotBlank() },
                    status = _state.value.status,
                    date = _state.value.date
                )
                
                orderRepository.updateOrder(updatedOrder)
                _events.emit(UiEvent.NavigateBack)
            } catch (e: Exception) {
                _error.value = "Ошибка обновления заказа: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Валидация полей формы перед отправкой
     */
    private fun validateInputs(): Boolean {
        var isValid = true
        val currentState = _state.value
        
        // Валидация названия
        if (currentState.title.isBlank()) {
            _state.value = currentState.copy(titleError = "Введите название заказа")
            isValid = false
        }
        
        // Валидация клиента
        if (currentState.client.isBlank()) {
            _state.value = currentState.copy(clientError = "Введите название клиента")
            isValid = false
        }
        
        // Валидация суммы
        val amountError = when {
            currentState.amount.isBlank() -> "Введите сумму заказа"
            currentState.amount.toDoubleOrNull() == null -> "Введите корректное число"
            currentState.amount.toDoubleOrNull()!! <= 0 -> "Сумма должна быть больше 0"
            else -> null
        }
        
        if (amountError != null) {
            _state.value = currentState.copy(amountError = amountError)
            isValid = false
        }
        
        // Валидация дохода, если он указан
        if (currentState.income.isNotBlank()) {
            val incomeError = when {
                currentState.income.toDoubleOrNull() == null -> "Введите корректное число"
                currentState.income.toDoubleOrNull()!! < 0 -> "Сумма не может быть отрицательной"
                else -> null
            }
            
            if (incomeError != null) {
                _state.value = currentState.copy(incomeError = incomeError)
                isValid = false
            }
        }
        
        return isValid
    }
    
    /**
     * Состояние формы заказа
     */
    data class OrderState(
        val title: String = "",
        val titleError: String? = null,
        
        val client: String = "",
        val clientError: String? = null,
        
        val amount: String = "",
        val amountError: String? = null,
        
        val income: String = "",
        val incomeError: String? = null,
        
        val notes: String = "",
        
        val status: OrderStatus = OrderStatus.PLANNED,
        val date: LocalDate = LocalDate.now()
    )
    
    /**
     * События UI
     */
    sealed class UiEvent {
        object NavigateBack : UiEvent()
    }
} 