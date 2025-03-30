package com.code1x5.rashod.data.repository

import com.code1x5.rashod.domain.model.Expense
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория для работы с расходами
 */
interface ExpenseRepository {
    /**
     * Получение всех расходов
     */
    fun getAllExpenses(): Flow<List<Expense>>
    
    /**
     * Получение расходов для заказа
     */
    fun getExpensesByOrderId(orderId: String): Flow<List<Expense>>
    
    /**
     * Получение расхода по ID
     */
    suspend fun getExpenseById(expenseId: String): Expense?
    
    /**
     * Добавление нового расхода
     */
    suspend fun addExpense(expense: Expense, orderId: String)
    
    /**
     * Добавление нескольких расходов
     */
    suspend fun addExpenses(expenses: List<Expense>, orderId: String)
    
    /**
     * Обновление расхода
     */
    suspend fun updateExpense(expense: Expense, orderId: String)
    
    /**
     * Удаление расхода
     */
    suspend fun deleteExpense(expense: Expense)
    
    /**
     * Удаление расхода по ID
     */
    suspend fun deleteExpenseById(expenseId: String)
    
    /**
     * Удаление всех расходов для заказа
     */
    suspend fun deleteExpensesByOrderId(orderId: String)
} 