package com.expensetracker.app.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.app.data.db.Expense
import com.expensetracker.app.service.PaymentNotificationListenerService
import com.expensetracker.app.service.QuickExpenseDialogActivity
import com.expensetracker.app.ui.components.CategoryBreakdownCard
import com.expensetracker.app.ui.components.ExpenseListItem
import com.expensetracker.app.ui.components.SummaryCard
import com.expensetracker.app.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }

    val hasNotificationAccess = remember(uiState) { isNotificationListenerEnabled(context) }
    val hasOverlayPermission = remember(uiState) { Settings.canDrawOverlays(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(
                        onClick = { simulatePayment(context, 20.0, "Google Pay") }
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Test Rs.20", fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!hasNotificationAccess) {
                item {
                    PermissionBannerCard(
                        icon = Icons.Default.Notifications,
                        title = "Enable Notification Access",
                        description = "Required to auto-detect UPI payment notifications.",
                        buttonText = "Open Settings",
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    )
                }
            }

            if (!hasOverlayPermission) {
                item {
                    PermissionBannerCard(
                        icon = Icons.Default.Warning,
                        title = "Allow Display Over Other Apps",
                        description = "Required to show the quick-entry popup over your screen.",
                        buttonText = "Grant Permission",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}")
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Today",
                        amount = uiState.todayTotal,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "This Month",
                        amount = uiState.monthTotal,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.categoryBreakdown.isNotEmpty()) {
                item {
                    CategoryBreakdownCard(
                        breakdown = uiState.categoryBreakdown,
                        monthTotal = uiState.monthTotal
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.expenses.size} items",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.expenses.isEmpty()) {
                item { EmptyStateCard() }
            } else {
                items(items = uiState.expenses, key = { it.id }) { expense ->
                    ExpenseListItem(
                        expense = expense,
                        onEdit = { editingExpense = it },
                        onDelete = { viewModel.deleteExpense(it) }
                    )
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    if (showAddDialog) {
        AddEditExpenseDialog(
            onSave = { amount, note, category, merchant ->
                viewModel.addExpense(amount, note, category, merchant)
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingExpense?.let { expense ->
        AddEditExpenseDialog(
            existingExpense = expense,
            onSave = { amount, note, category, merchant ->
                viewModel.updateExpense(
                    expense.copy(
                        amount = amount,
                        note = note,
                        category = category,
                        merchantOrSource = merchant
                    )
                )
            },
            onDismiss = { editingExpense = null }
        )
    }
}

@Composable
fun PermissionBannerCard(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = contentColor, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.8f))
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor,
                    contentColor = containerColor
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(buttonText, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("No expenses yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Tap + to add one, or tap 'Test Rs.20' to try the popup!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val cn = "${context.packageName}/${PaymentNotificationListenerService::class.java.canonicalName}"
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(cn) == true
}

private fun simulatePayment(context: Context, amount: Double, merchant: String) {
    val intent = Intent(context, QuickExpenseDialogActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        putExtra(PaymentNotificationListenerService.EXTRA_AMOUNT, amount)
        putExtra(PaymentNotificationListenerService.EXTRA_MERCHANT, merchant)
        putExtra(PaymentNotificationListenerService.EXTRA_RAW_TEXT, "Simulated payment of Rs.$amount via $merchant")
    }
    context.startActivity(intent)
}
