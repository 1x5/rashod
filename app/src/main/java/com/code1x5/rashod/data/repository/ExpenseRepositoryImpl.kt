package com.code1x5.rashod.data.repository

import android.util.Log
import com.code1x5.rashod.data.local.dao.ExpenseDao
import com.code1x5.rashod.data.local.entity.ExpenseEntity
import com.code1x5.rashod.domain.model.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация репозитория для работы с расходами
 */
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {
    
    companion object {
        private const val TAG = "ExpenseRepositoryImpl"
    }
    
    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses().map { expenses ->
            expenses.map { it.toDomain() }
        }
    }
    
    override fun getExpensesByOrderId(orderId: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByOrderId(orderId).map { expenses ->
            expenses.map { it.toDomain() }
        }
    }
    
    override suspend fun getExpenseById(expenseId: String): Expense? {
        return withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Получение расхода по ID: $expenseId")
                val expense = expenseDao.getExpenseById(expenseId)
                if (expense != null) {
                    Log.i(TAG, "Расход найден: ${expense.title}")
                    expense.toDomain()
                } else {
                    Log.w(TAG, "Расход с ID $expenseId не найден")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при получении расхода по ID: ${e.message}", e)
                null
            }
        }
    }
    
    override suspend fun addExpense(expense: Expense, orderId: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Начало добавления расхода: ${expense.id}, ${expense.title}, orderId: $orderId")
                // Проверяем, не существует ли уже расход с таким ID
                val existingExpense = expenseDao.getExpenseById(expense.id)
                if (existingExpense != null) {
                    Log.w(TAG, "Расход с ID ${expense.id} уже существует, будет перезаписан")
                }
                
                val expenseEntity = ExpenseEntity.fromDomain(expense, orderId)
                Log.i(TAG, "Сущность расхода создана успешно: ${expenseEntity.id}, ${expenseEntity.title}")
                
                val result = expenseDao.insertExpense(expenseEntity)
                Log.i(TAG, "Расход добавлен успешно, result: $result")
                
                // Проверяем, что расход действительно добавлен
                val checkExpense = expenseDao.getExpenseById(expense.id)
                if (checkExpense == null) {
                    Log.e(TAG, "Расход не был сохранен в БД, несмотря на успешную операцию")
                    throw RuntimeException("Расход не был сохранен в БД")
                } else {
                    Log.i(TAG, "Подтверждено: расход сохранен в БД: ${checkExpense.title}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при добавлении расхода: ${e.javaClass.simpleName} - ${e.message}", e)
                throw e
            }
        }
    }
    
    override suspend fun addExpenses(expenses: List<Expense>, orderId: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Начало добавления ${expenses.size} расходов для заказа $orderId")
                val expenseEntities = expenses.map { 
                    Log.d(TAG, "Подготовка расхода для БД: ${it.id}, ${it.title}")
                    ExpenseEntity.fromDomain(it, orderId) 
                }
                expenseDao.insertExpenses(expenseEntities)
                Log.i(TAG, "Расходы добавлены успешно, количество: ${expenses.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при добавлении расходов: ${e.message}", e)
                throw e
            }
        }
    }
    
    override suspend fun updateExpense(expense: Expense, orderId: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Начало обновления расхода: ${expense.id}, ${expense.title}")
                val expenseEntity = ExpenseEntity.fromDomain(expense, orderId)
                Log.i(TAG, "Сущность расхода для обновления создана успешно")
                
                expenseDao.updateExpense(expenseEntity)
                Log.i(TAG, "Расход обновлен успешно")
                
                // Проверяем, что расход действительно обновлен
                val checkExpense = expenseDao.getExpenseById(expense.id)
                if (checkExpense == null) {
                    Log.e(TAG, "Обновленный расход не найден в БД")
                    throw RuntimeException("Обновленный расход не найден в БД")
                } else {
                    Log.i(TAG, "Подтверждено: расход обновлен в БД: ${checkExpense.title}, сумма: ${checkExpense.amount}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при обновлении расхода: ${e.message}", e)
                throw e
            }
        }
    }
    
    override suspend fun deleteExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Начало удаления расхода: ${expense.id}, ${expense.title}")
                val entity = expenseDao.getExpenseById(expense.id)
                
                if (entity == null) {
                    Log.w(TAG, "Расход для удаления не найден: ${expense.id}")
                    return@withContext
                }
                
                expenseDao.deleteExpense(entity)
                Log.i(TAG, "Расход удален успешно")
                
                // Проверка удаления
                val check = expenseDao.getExpenseById(expense.id)
                if (check != null) {
                    Log.e(TAG, "Расход не был удален из БД: ${expense.id}")
                } else {
                    Log.i(TAG, "Подтверждено: расход удален из БД")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при удалении расхода: ${e.message}", e)
                throw e
            }
        }
    }
    
    override suspend fun deleteExpenseById(expenseId: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Начало удаления расхода по ID: $expenseId")
                expenseDao.deleteExpenseById(expenseId)
                Log.i(TAG, "Расход удален успешно")
                
                // Проверка удаления
                val check = expenseDao.getExpenseById(expenseId)
                if (check != null) {
                    Log.e(TAG, "Расход не был удален из БД: $expenseId")
                } else {
                    Log.i(TAG, "Подтверждено: расход удален из БД")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при удалении расхода по ID: ${e.message}", e)
                throw e
            }
        }
    }
    
    override suspend fun deleteExpensesByOrderId(orderId: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Начало удаления всех расходов для заказа: $orderId")
                expenseDao.deleteExpensesByOrderId(orderId)
                Log.i(TAG, "Все расходы для заказа удалены успешно")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при удалении расходов для заказа: ${e.message}", e)
                throw e
            }
        }
    }
} 