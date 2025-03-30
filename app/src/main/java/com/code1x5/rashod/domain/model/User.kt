package com.code1x5.rashod.domain.model

/**
 * Модель данных пользователя
 */
data class User(
    val email: String,
    val password: String,
    val isAuthenticated: Boolean = false,
    val company: String? = null,
    val position: String? = null,
    val name: String? = null
) 