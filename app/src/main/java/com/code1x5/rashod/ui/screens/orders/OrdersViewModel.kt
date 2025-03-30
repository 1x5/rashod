package com.code1x5.rashod.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.code1x5.rashod.data.repository.OrderRepository
import com.code1x5.rashod.domain.model.Order
import com.code1x5.rashod.domain.model.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана списка заказов
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    
    // Состояние поиска и фильтра
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText
    
    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen
    
    private val _selectedStatus = MutableStateFlow<OrderStatus?>(null)
    val selectedStatus: StateFlow<OrderStatus?> = _selectedStatus
    
    // Состояние для отображения меню фильтров
    private val _isFilterMenuOpen = MutableStateFlow(false)
    val isFilterMenuOpen: StateFlow<Boolean> = _isFilterMenuOpen
    
    // События навигации
    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()
    
    // Отфильтрованный список заказов
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val orders = combine(
        _searchText.debounce(300).distinctUntilChanged(),
        _selectedStatus
    ) { searchQuery, status ->
        Pair(searchQuery, status)
    }.flatMapLatest { (searchQuery, status) ->
        when {
            searchQuery.isBlank() && status == null -> {
                // Если нет поиска и фильтра, возвращаем все заказы
                orderRepository.getAllOrders()
            }
            searchQuery.isBlank() -> {
                // Если есть только фильтр по статусу
                orderRepository.getAllOrders().flatMapLatest { orders ->
                    flow {
                        emit(orders.filter { it.status == status })
                    }
                }
            }
            status == null -> {
                // Если есть только поиск
                orderRepository.searchOrders(searchQuery)
            }
            else -> {
                // Если есть и поиск, и фильтр
                orderRepository.searchOrders(searchQuery).flatMapLatest { orders ->
                    flow {
                        emit(orders.filter { it.status == status })
                    }
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Обработчики действий пользователя
    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }
    
    fun onOpenSearch() {
        _isSearchOpen.value = true
    }
    
    fun onCloseSearch() {
        _isSearchOpen.value = false
        _searchText.value = ""
    }
    
    fun onStatusFilterChange(status: OrderStatus?) {
        _selectedStatus.value = status
        // Закрываем меню фильтров после выбора
        _isFilterMenuOpen.value = false
    }
    
    // Переключаем состояние меню фильтров
    fun onToggleFilterMenu() {
        _isFilterMenuOpen.value = !_isFilterMenuOpen.value
    }
    
    // Закрываем меню фильтров
    fun onCloseFilterMenu() {
        _isFilterMenuOpen.value = false
    }
    
    /**
     * События UI для навигации
     */
    sealed class UiEvent {
        data class NavigateToOrderDetails(val orderId: String) : UiEvent()
    }
} 