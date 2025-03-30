package com.code1x5.rashod.ui.screens.edit_order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.code1x5.rashod.domain.model.OrderStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Экран добавления/редактирования заказа
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrderScreen(
    orderId: String?,
    onNavigateBack: () -> Unit,
    viewModel: EditOrderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(Unit) {
        if (orderId != null) {
            viewModel.loadOrder(orderId)
        }
    }
    
    // Обработка событий из ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditOrderViewModel.UiEvent.NavigateBack -> onNavigateBack()
            }
        }
    }
    
    val isEditMode = orderId != null
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (isEditMode) "Редактирование заказа" else "Новый заказ") 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    if (isEditMode) {
                        viewModel.updateOrder()
                    } else {
                        viewModel.createOrder()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Сохранить"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                OrderForm(
                    state = state,
                    onTitleChange = viewModel::updateTitle,
                    onClientChange = viewModel::updateClient,
                    onAmountChange = viewModel::updateAmount,
                    onIncomeChange = viewModel::updateIncome,
                    onNotesChange = viewModel::updateNotes,
                    onStatusChange = viewModel::updateStatus,
                    onDateChange = viewModel::updateDate,
                    isEditMode = isEditMode,
                    error = error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderForm(
    state: EditOrderViewModel.OrderState,
    onTitleChange: (String) -> Unit,
    onClientChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onIncomeChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onStatusChange: (OrderStatus) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    isEditMode: Boolean,
    error: String?
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val focusRequester = remember { FocusRequester() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Сообщение об ошибке, если есть
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        // Название заказа
        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = { Text("Название заказа*") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            isError = state.titleError != null,
            supportingText = {
                if (state.titleError != null) {
                    Text(state.titleError ?: "")
                }
            }
        )
        
        // Клиент
        OutlinedTextField(
            value = state.client,
            onValueChange = onClientChange,
            label = { Text("Клиент*") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.clientError != null,
            supportingText = {
                if (state.clientError != null) {
                    Text(state.clientError ?: "")
                }
            }
        )
        
        // Статус заказа
        Column {
            Text(
                text = "Статус заказа",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(
                    type = OrderStatus.PLANNED,
                    selected = state.status == OrderStatus.PLANNED,
                    onClick = { onStatusChange(OrderStatus.PLANNED) }
                )
                
                StatusChip(
                    type = OrderStatus.ACTIVE,
                    selected = state.status == OrderStatus.ACTIVE,
                    onClick = { onStatusChange(OrderStatus.ACTIVE) }
                )
                
                StatusChip(
                    type = OrderStatus.COMPLETED,
                    selected = state.status == OrderStatus.COMPLETED,
                    onClick = { onStatusChange(OrderStatus.COMPLETED) }
                )
            }
        }
        
        // Дата
        OutlinedTextField(
            value = state.date.format(dateFormatter),
            onValueChange = { /* Обрабатывается через DatePicker */ },
            label = { Text("Дата") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    // В реальном приложении здесь бы открывался DatePicker
                }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Выбрать дату"
                    )
                }
            }
        )
        
        // Сумма заказа
        OutlinedTextField(
            value = state.amount,
            onValueChange = onAmountChange,
            label = { Text("Сумма заказа*") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("₽") },
            isError = state.amountError != null,
            supportingText = {
                if (state.amountError != null) {
                    Text(state.amountError ?: "")
                }
            }
        )
        
        // Доход
        OutlinedTextField(
            value = state.income,
            onValueChange = onIncomeChange,
            label = { Text("Доход (если известен)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("₽") }
        )
        
        // Заметки
        OutlinedTextField(
            value = state.notes,
            onValueChange = onNotesChange,
            label = { Text("Заметки") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )
        
        // Кнопка удаления (только для режима редактирования)
        if (isEditMode) {
            Button(
                onClick = { /* Подтверждение удаления заказа */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Удалить заказ")
            }
        }
        
        Spacer(modifier = Modifier.height(72.dp)) // Место для FAB
    }
    
    LaunchedEffect(Unit) {
        if (!isEditMode) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
private fun StatusChip(
    type: OrderStatus,
    selected: Boolean,
    onClick: () -> Unit
) {
    val (backgroundColor, textColor, text) = when (type) {
        OrderStatus.PLANNED -> Triple(
            if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
            if (selected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.tertiary,
            "Планируемый"
        )
        OrderStatus.ACTIVE -> Triple(
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
            "Активный"
        )
        OrderStatus.COMPLETED -> Triple(
            if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
            if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.secondary,
            "Завершен"
        )
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        onClick = onClick
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
} 