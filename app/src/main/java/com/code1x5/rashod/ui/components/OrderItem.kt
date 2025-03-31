package com.code1x5.rashod.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.code1x5.rashod.domain.model.Order
import com.code1x5.rashod.domain.model.OrderStatus
import com.code1x5.rashod.ui.theme.*
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Компонент для отображения элемента заказа в списке
 */
@Composable
fun OrderItem(
    order: Order,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Верхняя строка с заголовком и статусом
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Заголовок заказа
                Text(
                    text = order.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Статус заказа
                StatusChip(status = order.status)
            }
            
            // Дата и клиент
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Дата: ${order.date.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Клиент: ${order.client}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            HorizontalDivider()
            
            // Финансовая информация
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Сумма заказа
                Column {
                    Text(
                        text = "Сумма:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = currencyFormatter.format(order.amount / 100.0),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Прибыль, если есть
                if (order.hasCompleteData) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Прибыль:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        val profitColor = when {
                            order.profit > 0 -> AccentPositive
                            order.profit < 0 -> AccentNegative
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currencyFormatter.format(order.profit / 100.0),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = profitColor
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            val profitPercentText = String.format("%.1f%%", order.profitPercent)
                            Text(
                                text = profitPercentText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = profitColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Компонент для отображения статуса заказа
 */
@Composable
fun StatusChip(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        OrderStatus.PLANNED -> Pair(StatusPlanned.copy(alpha = 0.1f), StatusPlanned)
        OrderStatus.ACTIVE -> Pair(StatusActive.copy(alpha = 0.1f), StatusActive)
        OrderStatus.COMPLETED -> Pair(Purple.copy(alpha = 0.1f), Purple)
    }
    
    val statusText = when (status) {
        OrderStatus.PLANNED -> "Планируемый"
        OrderStatus.ACTIVE -> "Активный"
        OrderStatus.COMPLETED -> "Завершенный"
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
} 