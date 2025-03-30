package com.code1x5.rashod.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val isFilterMenuOpen by viewModel.isFilterMenuOpen.collectAsState()
    
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
                        
                        // Иконка фильтра с подсказкой
                        Box {
                            IconButton(onClick = { 
                                // Показываем меню выбора статуса вместо циклического переключения
                                viewModel.onToggleFilterMenu()
                            }) {
                                val tint = when (selectedStatus) {
                                    null -> MaterialTheme.colorScheme.onSurface
                                    OrderStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                                    OrderStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                                    OrderStatus.PLANNED -> MaterialTheme.colorScheme.tertiary
                                }
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Фильтры",
                                    tint = tint
                                )
                            }
                            
                            // Показываем индикатор активного фильтра на иконке
                            if (selectedStatus != null) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = when (selectedStatus) {
                                                OrderStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                                                OrderStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                                                OrderStatus.PLANNED -> MaterialTheme.colorScheme.tertiary
                                                else -> MaterialTheme.colorScheme.surface
                                            },
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .align(Alignment.TopEnd)
                                )
                            }
                            
                            // Выпадающее меню фильтров
                            DropdownMenu(
                                expanded = isFilterMenuOpen,
                                onDismissRequest = { viewModel.onCloseFilterMenu() }
                            ) {
                                // Все заказы
                                DropdownMenuItem(
                                    onClick = { viewModel.onStatusFilterChange(null) },
                                    text = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (selectedStatus == null) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.width(24.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Все заказы")
                                        }
                                    }
                                )
                                
                                // Активные
                                DropdownMenuItem(
                                    onClick = { viewModel.onStatusFilterChange(OrderStatus.ACTIVE) },
                                    text = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (selectedStatus == OrderStatus.ACTIVE) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.width(24.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Активные")
                                        }
                                    }
                                )
                                
                                // Завершенные
                                DropdownMenuItem(
                                    onClick = { viewModel.onStatusFilterChange(OrderStatus.COMPLETED) },
                                    text = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (selectedStatus == OrderStatus.COMPLETED) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.width(24.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Завершенные")
                                        }
                                    }
                                )
                                
                                // Планируемые
                                DropdownMenuItem(
                                    onClick = { viewModel.onStatusFilterChange(OrderStatus.PLANNED) },
                                    text = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (selectedStatus == OrderStatus.PLANNED) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.width(24.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Планируемые")
                                        }
                                    }
                                )
                            }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Показываем индикатор активного фильтра, если он выбран
            if (selectedStatus != null) {
                val chipText = when (selectedStatus) {
                    OrderStatus.ACTIVE -> "Активные"
                    OrderStatus.COMPLETED -> "Завершенные"
                    OrderStatus.PLANNED -> "Планируемые"
                    else -> ""
                }
                val chipColor = when (selectedStatus) {
                    OrderStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                    OrderStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                    OrderStatus.PLANNED -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.surface
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Создаем собственный чип вместо SuggestionChip
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(chipColor.copy(alpha = 0.2f))
                            .clickable { viewModel.onStatusFilterChange(null) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = chipText,
                                color = chipColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Очистить фильтр",
                                tint = chipColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyOrdersList()
                }
            } else {
                OrdersList(
                    orders = orders,
                    onOrderClick = { onOrderClick(it.id) }
                )
            }
        }
    }
}

@Composable
private fun EmptyOrdersList() {
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

@Composable
private fun OrdersList(
    orders: List<Order>,
    onOrderClick: (Order) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
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
    // Используем более простую версию SearchBar
    TopAppBar(
        title = {
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                placeholder = { Text("Поиск по заказу или клиенту...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск"
                    )
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { onSearchTextChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Очистить"
                            )
                        }
                    }
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseSearch) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад"
                )
            }
        }
    )
} 