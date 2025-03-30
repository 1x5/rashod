package com.code1x5.rashod.ui.screens.edit_expense

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.code1x5.rashod.domain.model.ExpenseCategory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "EditExpenseScreen"

/**
 * Экран добавления/редактирования расхода
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    orderId: String,
    expenseId: String?,
    onNavigateBack: () -> Unit,
    viewModel: EditExpenseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(Unit) {
        Log.i(TAG, "Initializing ViewModel with orderId: $orderId, expenseId: $expenseId")
        viewModel.initialize(orderId, expenseId)
    }
    
    // Отслеживаем события из ViewModel
    LaunchedEffect(key1 = Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditExpenseViewModel.UiEvent.NavigateBack -> {
                    Log.i(TAG, "Received NavigateBack event, returning to previous screen")
                    onNavigateBack()
                }
            }
        }
    }
    
    val isEditMode = expenseId != null
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Редактирование расхода" else "Новый расход")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.i(TAG, "FAB clicked, isEditMode: $isEditMode")
                    if (isEditMode) {
                        viewModel.updateExpense()
                    } else {
                        viewModel.createExpense()
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
                ExpenseForm(
                    state = state,
                    onTitleChange = viewModel::updateTitle,
                    onAmountChange = viewModel::updateAmount,
                    onCategoryChange = viewModel::updateCategory,
                    onNotesChange = viewModel::updateNotes,
                    isEditMode = isEditMode,
                    error = error,
                    onDeleteClick = {
                        if (isEditMode) {
                            viewModel.deleteExpense()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseForm(
    state: EditExpenseViewModel.ExpenseState,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (ExpenseCategory) -> Unit,
    onNotesChange: (String) -> Unit,
    isEditMode: Boolean,
    error: String?,
    onDeleteClick: () -> Unit
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
        
        // Название расхода
        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = { Text("Название расхода*") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            isError = state.titleError != null,
            supportingText = {
                if (state.titleError != null) {
                    Text(state.titleError)
                }
            }
        )
        
        // Категория расхода
        Column {
            Text(
                text = "Категория расхода",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExpenseCategory.values().forEach { category ->
                    CategoryChip(
                        category = category,
                        selected = state.category == category,
                        onClick = { onCategoryChange(category) }
                    )
                }
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
        
        // Сумма расхода
        OutlinedTextField(
            value = state.amount,
            onValueChange = onAmountChange,
            label = { Text("Сумма расхода*") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("₽") },
            isError = state.amountError != null,
            supportingText = {
                if (state.amountError != null) {
                    Text(state.amountError)
                }
            }
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
                onClick = onDeleteClick,
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
                Text("Удалить расход")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChip(
    category: ExpenseCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val categoryName = when (category) {
        ExpenseCategory.MATERIALS -> "Материалы"
        ExpenseCategory.TOOLS -> "Инструменты"
        ExpenseCategory.TRANSPORT -> "Транспорт"
        ExpenseCategory.FOOD -> "Питание"
        ExpenseCategory.OTHER -> "Прочее"
    }
    
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(categoryName) },
        leadingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
            }
        }
    )
} 