package com.code1x5.rashod.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.code1x5.rashod.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана настроек
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    // TODO: Добавить UserRepository для работы с данными пользователя
) : ViewModel() {
    
    // Данные пользователя
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Состояние ошибки
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Состояние входа
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    // Текущая тема приложения (для примера)
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    
    init {
        // Здесь бы загружали данные пользователя из репозитория
        // Для примера, создадим пользователя
        _user.value = User(
            email = "test@example.com",
            password = "password",
            isAuthenticated = true
        )
    }
    
    /**
     * Обновление email
     */
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }
    
    /**
     * Обновление пароля
     */
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }
    
    /**
     * Вход в систему
     */
    fun login() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // TODO: Реализовать вход в систему через репозиторий
                // Для примера, просто обновляем пользователя
                _user.value = User(
                    email = _email.value,
                    password = _password.value,
                    isAuthenticated = true
                )
            } catch (e: Exception) {
                _error.value = "Ошибка входа: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Выход из системы
     */
    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // TODO: Реализовать выход из системы через репозиторий
                // Для примера, просто обновляем пользователя
                _user.value = User(
                    email = "",
                    password = "",
                    isAuthenticated = false
                )
                _email.value = ""
                _password.value = ""
            } catch (e: Exception) {
                _error.value = "Ошибка выхода: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Переключение темы
     */
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }
} 