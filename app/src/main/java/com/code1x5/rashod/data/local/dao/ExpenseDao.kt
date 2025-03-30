package com.code1x5.rashod.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.code1x5.rashod.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с расходами
 */
@Dao
interface ExpenseDao {
    /**
     * Получение всех расходов
     */
    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    
    /**
     * Получение расходов по ID заказа
     */
    @Query("SELECT * FROM expenses WHERE orderId = :orderId")
    fun getExpensesByOrderId(orderId: String): Flow<List<ExpenseEntity>>
    
    /**
     * Получение расхода по ID
     */
    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: String): ExpenseEntity?
    
    /**
     * Вставка нового расхода
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long
    
    /**
     * Вставка нескольких расходов
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)
    
    /**
     * Обновление расхода
     */
    @Update
    suspend fun updateExpense(expense: ExpenseEntity)
    
    /**
     * Удаление расхода
     */
    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
    
    /**
     * Удаление расхода по ID
     */
    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: String)
    
    /**
     * Удаление всех расходов для заказа
     */
    @Query("DELETE FROM expenses WHERE orderId = :orderId")
    suspend fun deleteExpensesByOrderId(orderId: String)
} 