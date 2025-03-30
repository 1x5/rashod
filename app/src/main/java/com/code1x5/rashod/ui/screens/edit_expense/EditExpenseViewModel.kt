package com.code1x5.rashod.ui.screens.edit_expense

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.code1x5.rashod.data.repository.ExpenseRepository
import com.code1x5.rashod.domain.model.Expense
import com.code1x5.rashod.domain.model.ExpenseCategory
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
 * ViewModel для экрана добавления/редактирования расхода
 */
@HiltViewModel
class EditExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "EditExpenseViewModel"
    }
    
    private val _state = MutableStateFlow(ExpenseState())
    val state: StateFlow<ExpenseState> = _state.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()
    
    // ID редактируемого расхода и ID заказа
    private var expenseId: String? = null
    private var orderId: String? = null
    
    /**
     * Инициализация ViewModel с ID заказа и опционально ID расхода
     */
    fun initialize(orderId: String, expenseId: String?) {
        Log.i(TAG, "Инициализация ViewModel с orderId: $orderId, expenseId: $expenseId")
        this.orderId = orderId
        
        if (expenseId != null) {
            this.expenseId = expenseId
            loadExpense(expenseId)
        }
    }
    
    /**
     * Загрузка данных расхода для редактирования
     */
    private fun loadExpense(id: String) {
        Log.i(TAG, "Начало загрузки расхода с ID: $id")
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val expense = expenseRepository.getExpenseById(id)
                if (expense != null) {
                    Log.i(TAG, "Расход загружен успешно: ${expense.title}, сумма: ${expense.amount}")
                    _state.value = ExpenseState(
                        title = expense.title,
                        amount = (expense.amount / 100.0).toString(),
                        notes = expense.notes ?: "",
                        category = expense.category,
                        date = expense.date
                    )
                } else {
                    Log.e(TAG, "Расход с ID $id не найден")
                    _error.value = "Расход не найден"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка загрузки расхода: ${e.message}", e)
                _error.value = "Ошибка загрузки расхода: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Обновление полей расхода
     */
    fun updateTitle(title: String) {
        _state.value = _state.value.copy(
            title = title,
            titleError = if (title.isBlank()) "Введите название расхода" else null
        )
    }
    
    fun updateAmount(amount: String) {
        val amountError = when {
            amount.isBlank() -> "Введите сумму расхода"
            amount.toDoubleOrNull() == null -> "Введите корректное число"
            amount.toDoubleOrNull()!! <= 0 -> "Сумма должна быть больше 0"
            else -> null
        }
        
        _state.value = _state.value.copy(
            amount = amount,
            amountError = amountError
        )
    }
    
    fun updateCategory(category: ExpenseCategory) {
        _state.value = _state.value.copy(category = category)
    }
    
    fun updateNotes(notes: String) {
        _state.value = _state.value.copy(notes = notes)
    }
    
    fun updateDate(date: LocalDate) {
        _state.value = _state.value.copy(date = date)
    }
    
    /**
     * Создание нового расхода
     */
    fun createExpense() {
        Log.i(TAG, "Вызов createExpense(), orderId: $orderId")
        
        if (!validateInputs()) {
            Log.e(TAG, "Проверка полей не прошла")
            return
        }
        
        if (orderId == null) {
            Log.e(TAG, "orderId равен null, невозможно создать расход")
            _error.value = "Не указан ID заказа"
            return
        }
        
        viewModelScope.launch {
            Log.i(TAG, "Начало создания расхода")
            _isLoading.value = true
            _error.value = null
            
            try {
                // Преобразуем значения в копейки для хранения в БД
                val amountString = _state.value.amount
                Log.i(TAG, "Строковое значение суммы: $amountString")
    
                val amountDouble = amountString.toDoubleOrNull()
                if (amountDouble == null) {
                    Log.e(TAG, "Ошибка преобразования суммы в число")
                    _error.value = "Неверный формат суммы"
                    _isLoading.value = false
                    return@launch
                }
                
                val amountInCents = (amountDouble * 100).toLong()
                Log.i(TAG, "Сумма в копейках: $amountInCents")
                
                val expenseId = UUID.randomUUID().toString()
                Log.i(TAG, "Создан ID для расхода: $expenseId")
                
                val newExpense = Expense(
                    id = expenseId,
                    title = _state.value.title,
                    amount = amountInCents,
                    notes = _state.value.notes.takeIf { it.isNotBlank() },
                    category = _state.value.category,
                    date = _state.value.date
                )
                
                Log.i(TAG, "Расход создан в модели: ${newExpense.id}, ${newExpense.title}, ${newExpense.amount}")
                
                try {
                    // Используем forceful подход - пытаемся сохранить несколько раз если нужно
                    var saveSuccess = false
                    var attempts = 0
                    var lastError: Exception? = null
                    
                    while (!saveSuccess && attempts < 3) {
                        try {
                            attempts++
                            Log.i(TAG, "Попытка сохранения расхода #$attempts")
                            expenseRepository.addExpense(newExpense, orderId!!)
                            saveSuccess = true
                            Log.i(TAG, "Расход успешно сохранен в репозитории")
                        } catch (e: Exception) {
                            Log.e(TAG, "Ошибка сохранения расхода, попытка #$attempts: ${e.message}", e)
                            lastError = e
                            // Небольшая задержка перед повторной попыткой
                            kotlinx.coroutines.delay(300)
                        }
                    }
                    
                    if (saveSuccess) {
                        // Добавляем задержку перед навигацией назад, чтобы Room успел завершить транзакцию
                        kotlinx.coroutines.delay(500)
                        Log.i(TAG, "Отправка события NavigateBack после успешного сохранения")
                        _events.emit(UiEvent.NavigateBack)
                    } else {
                        // Если все попытки не удались, выводим ошибку
                        Log.e(TAG, "Все попытки сохранения расхода не удались", lastError)
                        _error.value = "Ошибка сохранения расхода после нескольких попыток: ${lastError?.message}"
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Критическая ошибка сохранения расхода в репозитории: ${e.message}", e)
                    _error.value = "Критическая ошибка сохранения расхода: ${e.message}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка создания расхода: ${e.javaClass.simpleName} - ${e.message}", e)
                _error.value = "Ошибка создания расхода: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Обновление существующего расхода
     */
    fun updateExpense() {
        Log.i(TAG, "Вызов updateExpense(), expenseId: $expenseId, orderId: $orderId")
        
        if (!validateInputs()) {
            Log.e(TAG, "Проверка полей не прошла")
            return
        }
        
        if (expenseId == null || orderId == null) {
            Log.e(TAG, "expenseId или orderId равен null, невозможно обновить расход")
            _error.value = "Не указан ID расхода или заказа"
            return
        }
        
        viewModelScope.launch {
            Log.i(TAG, "Начало обновления расхода")
            _isLoading.value = true
            _error.value = null
            
            try {
                val amountString = _state.value.amount
                val amountDouble = amountString.toDoubleOrNull()
                
                if (amountDouble == null) {
                    _error.value = "Неверный формат суммы"
                    _isLoading.value = false
                    return@launch
                }
                
                val amountInCents = (amountDouble * 100).toLong()
                
                val updatedExpense = Expense(
                    id = expenseId!!,
                    title = _state.value.title,
                    amount = amountInCents,
                    notes = _state.value.notes.takeIf { it.isNotBlank() },
                    category = _state.value.category,
                    date = _state.value.date
                )
                
                Log.i(TAG, "Обновление расхода: ${updatedExpense.id}, ${updatedExpense.title}, ${updatedExpense.amount}")
                
                try {
                    // Используем тот же подход с повторными попытками
                    var saveSuccess = false
                    var attempts = 0
                    var lastError: Exception? = null
                    
                    while (!saveSuccess && attempts < 3) {
                        try {
                            attempts++
                            Log.i(TAG, "Попытка обновления расхода #$attempts")
                            expenseRepository.updateExpense(updatedExpense, orderId!!)
                            saveSuccess = true
                            Log.i(TAG, "Расход успешно обновлен в репозитории")
                        } catch (e: Exception) {
                            Log.e(TAG, "Ошибка обновления расхода, попытка #$attempts: ${e.message}", e)
                            lastError = e
                            // Небольшая задержка перед повторной попыткой
                            kotlinx.coroutines.delay(300)
                        }
                    }
                    
                    if (saveSuccess) {
                        // Добавляем задержку перед навигацией назад
                        kotlinx.coroutines.delay(500)
                        Log.i(TAG, "Отправка события NavigateBack после успешного обновления")
                        _events.emit(UiEvent.NavigateBack)
                    } else {
                        // Если все попытки не удались, выводим ошибку
                        Log.e(TAG, "Все попытки обновления расхода не удались", lastError)
                        _error.value = "Ошибка обновления расхода после нескольких попыток: ${lastError?.message}"
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Критическая ошибка обновления расхода: ${e.message}", e)
                    _error.value = "Критическая ошибка обновления расхода: ${e.message}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка подготовки данных для обновления: ${e.message}", e)
                _error.value = "Ошибка обновления расхода: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Удаление расхода
     */
    fun deleteExpense() {
        Log.i(TAG, "Вызов deleteExpense(), expenseId: $expenseId")
        
        if (expenseId == null) {
            Log.e(TAG, "expenseId равен null, невозможно удалить расход")
            _error.value = "Не указан ID расхода"
            return
        }
        
        viewModelScope.launch {
            Log.i(TAG, "Начало удаления расхода")
            _isLoading.value = true
            _error.value = null
            
            try {
                expenseRepository.deleteExpenseById(expenseId!!)
                Log.i(TAG, "Расход успешно удален")
                _events.emit(UiEvent.NavigateBack)
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка удаления расхода: ${e.message}", e)
                _error.value = "Ошибка удаления расхода: ${e.message}"
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
            _state.value = currentState.copy(titleError = "Введите название расхода")
            isValid = false
            Log.e(TAG, "Ошибка валидации: пустое название")
        }
        
        // Валидация суммы
        val amountError = when {
            currentState.amount.isBlank() -> "Введите сумму расхода"
            currentState.amount.toDoubleOrNull() == null -> "Введите корректное число"
            currentState.amount.toDoubleOrNull()!! <= 0 -> "Сумма должна быть больше 0"
            else -> null
        }
        
        if (amountError != null) {
            _state.value = currentState.copy(amountError = amountError)
            isValid = false
            Log.e(TAG, "Ошибка валидации суммы: $amountError")
        }
        
        Log.i(TAG, "Результат валидации: $isValid")
        return isValid
    }
    
    /**
     * Состояние формы расхода
     */
    data class ExpenseState(
        val title: String = "",
        val titleError: String? = null,
        
        val amount: String = "",
        val amountError: String? = null,
        
        val notes: String = "",
        
        val category: ExpenseCategory = ExpenseCategory.MATERIALS,
        val date: LocalDate = LocalDate.now()
    )
    
    /**
     * События UI
     */
    sealed class UiEvent {
        object NavigateBack : UiEvent()
    }
} 