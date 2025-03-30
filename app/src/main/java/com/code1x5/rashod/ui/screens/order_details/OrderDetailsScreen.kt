package com.code1x5.rashod.ui.screens.order_details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.code1x5.rashod.domain.model.Expense
import com.code1x5.rashod.domain.model.ExpenseCategory
import com.code1x5.rashod.domain.model.Order
import com.code1x5.rashod.domain.model.OrderStatus
import com.code1x5.rashod.ui.components.StatusChip
import com.code1x5.rashod.ui.theme.Green
import com.code1x5.rashod.ui.theme.Red
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Экран деталей заказа
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onEditOrderClick: (String) -> Unit,
    onAddExpenseClick: (String) -> Unit,
    onExpenseClick: (String, String) -> Unit,
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    val order by viewModel.order.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is OrderDetailsViewModel.UiEvent.NavigateToEditOrder -> {
                    onEditOrderClick(event.orderId)
                }
                is OrderDetailsViewModel.UiEvent.NavigateToEditExpense -> {
                    if (event.expenseId != null) {
                        onExpenseClick(event.orderId, event.expenseId)
                    } else {
                        onAddExpenseClick(event.orderId)
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали заказа") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEditOrderClick(orderId) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать заказ"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddExpenseClick(orderId) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить расход"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "Неизвестная ошибка",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Вернуться назад")
                        }
                    }
                }
                order != null -> {
                    OrderDetailsContent(
                        order = order!!,
                        expenses = expenses,
                        photos = photos,
                        onExpenseClick = { expenseId -> onExpenseClick(orderId, expenseId) },
                        paddingValues = paddingValues
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Заказ не найден")
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetailsContent(
    order: Order,
    expenses: List<Expense>,
    photos: List<String>,
    onExpenseClick: (String) -> Unit,
    paddingValues: PaddingValues
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Заголовок и статус заказа
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                StatusChip(status = order.status)
            }
        }
        
        // Основная информация о заказе
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoRow(label = "Клиент:", value = order.client)
                    InfoRow(label = "Дата:", value = order.date.format(dateFormatter))
                    InfoRow(
                        label = "Сумма заказа:",
                        value = currencyFormatter.format(order.amount / 100.0)
                    )
                    if (order.income != null) {
                        InfoRow(
                            label = "Доход:",
                            value = currencyFormatter.format(order.income / 100.0)
                        )
                    }
                    InfoRow(
                        label = "Расходы:",
                        value = currencyFormatter.format(order.totalExpenses / 100.0)
                    )
                    
                    if (order.hasCompleteData) {
                        val profitColor = when {
                            order.profit > 0 -> MaterialTheme.colorScheme.primary
                            order.profit < 0 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Прибыль:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currencyFormatter.format(order.profit / 100.0),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = profitColor
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Рентабельность:",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = String.format("%.1f%%", order.profitPercent),
                                style = MaterialTheme.typography.bodyLarge,
                                color = profitColor
                            )
                        }
                    }
                }
            }
        }
        
        // Заметки
        if (!order.notes.isNullOrBlank()) {
            item {
                SectionTitle(title = "Заметки")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = order.notes.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        
        // Фотографии
        if (photos.isNotEmpty()) {
            item {
                SectionTitle(title = "Фотографии")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)
                ) {
                    items(photos) { photoPath ->
                        PhotoItem(photoPath = photoPath)
                    }
                }
            }
        }
        
        // Расходы
        item {
            SectionTitle(title = "Расходы")
        }
        
        if (expenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет расходов",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            items(expenses) { expense ->
                ExpenseItem(
                    expense = expense,
                    currencyFormatter = currencyFormatter,
                    dateFormatter = dateFormatter,
                    onClick = { onExpenseClick(expense.id) }
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun PhotoItem(
    photoPath: String,
    modifier: Modifier = Modifier
) {
    // В реальном приложении здесь бы отображалась фотография из пути photoPath
    // Для простоты примера, используем заглушку
    Box(
        modifier = modifier
            .size(120.dp, 120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        // Для отладки показываем имя файла без полного пути
        val fileName = photoPath.substringAfterLast('/', "image")
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            // Отображаем имя файла
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseItem(
    expense: Expense,
    currencyFormatter: NumberFormat,
    dateFormatter: DateTimeFormatter,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = getCategoryName(expense.category),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = expense.date.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = currencyFormatter.format(expense.amount / 100.0),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun getCategoryName(category: ExpenseCategory): String {
    return when (category) {
        ExpenseCategory.MATERIALS -> "Материалы"
        ExpenseCategory.TOOLS -> "Инструменты"
        ExpenseCategory.TRANSPORT -> "Транспорт"
        ExpenseCategory.FOOD -> "Питание"
        ExpenseCategory.OTHER -> "Прочее"
    }
} 