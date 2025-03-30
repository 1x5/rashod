package com.code1x5.rashod.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.code1x5.rashod.domain.model.Order
import com.code1x5.rashod.domain.model.OrderStatus
import com.code1x5.rashod.ui.components.OrderItem
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Экран списка заказов
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreen(
    onOrderClick: (String) -> Unit,
    onAddOrderClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState(initial = emptyList())
    val searchText by viewModel.searchText.collectAsState()
    val isSearchOpen by viewModel.isSearchOpen.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is OrdersViewModel.UiEvent.NavigateToOrderDetails -> {
                    onOrderClick(event.orderId)
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            if (isSearchOpen) {
                SearchTopBar(
                    searchText = searchText,
                    onSearchTextChange = viewModel::onSearchTextChange,
                    onCloseSearch = viewModel::onCloseSearch
                )
            } else {
                TopAppBar(
                    title = { Text("Мои заказы") },
                    actions = {
                        IconButton(onClick = viewModel::onOpenSearch) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Поиск"
                            )
                        }
                        IconButton(onClick = { /* TODO: Открыть диалог фильтров */ }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Фильтры"
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Настройки"
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddOrderClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить заказ"
                )
            }
        }
    ) { paddingValues ->
        if (orders.isEmpty()) {
            EmptyOrdersList(paddingValues = paddingValues)
        } else {
            OrdersList(
                orders = orders,
                onOrderClick = { onOrderClick(it.id) },
                paddingValues = paddingValues
            )
        }
    }
}

@Composable
private fun EmptyOrdersList(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "У вас пока нет заказов",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Нажмите + чтобы добавить новый заказ",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun OrdersList(
    orders: List<Order>,
    onOrderClick: (Order) -> Unit,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(orders) { order ->
            OrderItem(
                order = order,
                onClick = { onOrderClick(order) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onCloseSearch: () -> Unit
) {
    SearchBar(
        query = searchText,
        onQueryChange = onSearchTextChange,
        onSearch = { },
        active = true,
        onActiveChange = { },
        leadingIcon = {
            IconButton(onClick = onCloseSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Поиск"
                )
            }
        },
        trailingIcon = {
            IconButton(onClick = { onSearchTextChange("") }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Очистить"
                )
            }
        },
        placeholder = {
            Text("Поиск по заказу или клиенту...")
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        // Контент поиска, но для простоты пока оставим пустым
    }
} 