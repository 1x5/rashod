package com.code1x5.rashod.di

import com.code1x5.rashod.data.repository.ExpenseRepository
import com.code1x5.rashod.data.repository.ExpenseRepositoryImpl
import com.code1x5.rashod.data.repository.OrderRepository
import com.code1x5.rashod.data.repository.OrderRepositoryImpl
import com.code1x5.rashod.data.repository.PhotoRepository
import com.code1x5.rashod.data.repository.PhotoRepositoryImpl
import com.code1x5.rashod.data.repository.ThemeRepository
import com.code1x5.rashod.data.repository.ThemeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Модуль Hilt для внедрения зависимостей репозиториев
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * Привязка реализации репозитория заказов к интерфейсу
     */
    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository
    
    /**
     * Привязка реализации репозитория расходов к интерфейсу
     */
    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository
    
    /**
     * Привязка реализации репозитория фотографий к интерфейсу
     */
    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        photoRepositoryImpl: PhotoRepositoryImpl
    ): PhotoRepository
    
    /**
     * Привязка реализации репозитория темы к интерфейсу
     */
    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        themeRepositoryImpl: ThemeRepositoryImpl
    ): ThemeRepository
} 