package com.expensetracker.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.app.data.db.CategoryTotal
import com.expensetracker.app.utils.CategoryMapper
import com.expensetracker.app.utils.DateUtils

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor.copy(alpha = 0.7f)
            )
            Text(
                text = DateUtils.formatAmount(amount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )
        }
    }
}

@Composable
fun CategoryBreakdownCard(
    breakdown: List<CategoryTotal>,
    monthTotal: Double,
    modifier: Modifier = Modifier
) {
    if (breakdown.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "This Month by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            breakdown.forEach { item ->
                val fraction = if (monthTotal > 0) (item.total / monthTotal).toFloat() else 0f
                CategoryProgressRow(
                    category = item.category,
                    amount = item.total,
                    fraction = fraction.coerceIn(0f, 1f)
                )
            }
        }
    }
}

@Composable
fun CategoryProgressRow(
    category: String,
    amount: Double,
    fraction: Float
) {
    val color = categoryColor(category)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${CategoryMapper.categoryIcon(category)} $category",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = DateUtils.formatAmount(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

fun categoryColor(category: String): Color {
    return when (category) {
        "Food & Drinks" -> Color(0xFFE64A19)
        "Travel & Fuel" -> Color(0xFF1976D2)
        "Groceries" -> Color(0xFF388E3C)
        "Bills & Utilities" -> Color(0xFFF57C00)
        "Shopping" -> Color(0xFF7B1FA2)
        else -> Color(0xFF546E7A)
    }
}
