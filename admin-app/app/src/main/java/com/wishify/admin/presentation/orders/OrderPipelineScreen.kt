package com.wishify.admin.presentation.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wishify.admin.data.model.AdminOrder
import com.wishify.admin.data.repository.WishifyAdminRepository
import com.wishify.admin.ui.theme.GoldAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderPipelineScreen(
    repository: WishifyAdminRepository,
    modifier: Modifier = Modifier
) {
    val statuses = listOf("ALL", "PLACED", "CONFIRMED", "PREPARED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED")
    var selectedStatus by remember { mutableStateOf("ALL") }
    var ordersState by remember { mutableStateOf<Result<List<AdminOrder>>?>(null) }
    var selectedOrderForAction by remember { mutableStateOf<AdminOrder?>(null) }
    var showAssignDialog by remember { mutableStateOf(false) }

    var partnerName by remember { mutableStateOf("Ramesh Kumar (Shadowfax)") }
    var partnerPhone by remember { mutableStateOf("+919876500000") }
    var trackingNumber by remember { mutableStateOf("TRK-789012") }

    val scope = rememberCoroutineScope()

    val fetchOrders = {
        scope.launch {
            ordersState = null
            val statusParam = if (selectedStatus == "ALL") null else selectedStatus
            ordersState = repository.getOrders(statusParam)
        }
    }

    LaunchedEffect(selectedStatus) {
        fetchOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Fulfillment Pipeline 🚚", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { fetchOrders() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GoldAccent)
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Status Tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statuses) { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(status.replace("_", " ")) }
                    )
                }
            }

            val state = ordersState
            when {
                state == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldAccent)
                    }
                }
                state.isFailure -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Failed to load orders: ${state.exceptionOrNull()?.message}", color = Color.Red)
                    }
                }
                state.isSuccess -> {
                    val orders = state.getOrNull()!!
                    if (orders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No orders in this status", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(orders) { order ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedOrderForAction = order }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(order.orderNumber, fontWeight = FontWeight.Bold)
                                            StatusBadge(order.orderStatus)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Recipient: ${order.recipientName} (${order.recipientPhone})", fontSize = 13.sp)
                                        Text("Delivery Date: ${order.deliveryDate.take(10)}", fontSize = 13.sp, color = Color.Gray)
                                        if (order.giftMessage != null) {
                                            Text("Gift Message: \"${order.giftMessage}\"", fontSize = 12.sp, color = GoldAccent)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${order.items.size} item(s) • ${order.paymentMethod}", fontSize = 12.sp, color = Color.Gray)
                                            Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = GoldAccent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Bottom Sheet / Dialog for advancing order status
        selectedOrderForAction?.let { order ->
            AlertDialog(
                onDismissRequest = { selectedOrderForAction = null },
                title = { Text("Manage ${order.orderNumber}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Current Status: ${order.orderStatus}", fontWeight = FontWeight.SemiBold)
                        Text("Recipient: ${order.recipientName}", fontSize = 13.sp)
                        Text("Total: ₹${order.totalAmount.toInt()} (${order.paymentStatus})", fontSize = 13.sp)
                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        when (order.orderStatus) {
                            "PLACED" -> {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            repository.updateOrderStatus(order.id, "CONFIRMED")
                                            selectedOrderForAction = null
                                            fetchOrders()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Accept & Confirm Order")
                                }
                            }
                            "CONFIRMED" -> {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            repository.updateOrderStatus(order.id, "PREPARED")
                                            selectedOrderForAction = null
                                            fetchOrders()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Mark as Prepared & Packed")
                                }
                            }
                            "PREPARED" -> {
                                Button(
                                    onClick = {
                                        showAssignDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Assign Delivery & Dispatch")
                                }
                            }
                            "OUT_FOR_DELIVERY" -> {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            repository.updateOrderStatus(order.id, "DELIVERED")
                                            selectedOrderForAction = null
                                            fetchOrders()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Mark as Successfully Delivered")
                                }
                            }
                        }

                        if (order.orderStatus != "CANCELLED" && order.orderStatus != "DELIVERED") {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        repository.updateOrderStatus(order.id, "CANCELLED", "Admin cancelled")
                                        selectedOrderForAction = null
                                        fetchOrders()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Text("Cancel Order")
                            }
                        }

                        if (order.paymentStatus == "PAID") {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        repository.refundOrder(order.id)
                                        selectedOrderForAction = null
                                        fetchOrders()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Process Refund")
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { selectedOrderForAction = null }) {
                        Text("Close")
                    }
                }
            )
        }

        // Assign Delivery Partner Dialog
        if (showAssignDialog && selectedOrderForAction != null) {
            AlertDialog(
                onDismissRequest = { showAssignDialog = false },
                title = { Text("Dispatch Order") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = partnerName,
                            onValueChange = { partnerName = it },
                            label = { Text("Delivery Partner Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = partnerPhone,
                            onValueChange = { partnerPhone = it },
                            label = { Text("Partner Phone") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = trackingNumber,
                            onValueChange = { trackingNumber = it },
                            label = { Text("Tracking Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                repository.assignDelivery(
                                    selectedOrderForAction!!.id,
                                    partnerName,
                                    partnerPhone,
                                    trackingNumber
                                )
                                repository.updateOrderStatus(selectedOrderForAction!!.id, "OUT_FOR_DELIVERY")
                                showAssignDialog = false
                                selectedOrderForAction = null
                                fetchOrders()
                            }
                        }
                    ) {
                        Text("Dispatch")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAssignDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "PLACED" -> Color(0xFF1565C0).copy(alpha = 0.2f) to Color(0xFF64B5F6)
        "CONFIRMED" -> Color(0xFF00838F).copy(alpha = 0.2f) to Color(0xFF4DD0E1)
        "PREPARED" -> Color(0xFFE65100).copy(alpha = 0.2f) to Color(0xFFFFB74D)
        "OUT_FOR_DELIVERY" -> Color(0xFF2E7D32).copy(alpha = 0.2f) to Color(0xFF81C784)
        "DELIVERED" -> Color(0xFF1B5E20).copy(alpha = 0.2f) to Color(0xFFA5D6A7)
        "CANCELLED" -> Color(0xFFC62828).copy(alpha = 0.2f) to Color(0xFFEF9A9A)
        else -> Color.Gray.copy(alpha = 0.2f) to Color.White
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = status.replace("_", " "),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
