package com.wishify.customer.presentation.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wishify.customer.data.model.Order
import com.wishify.customer.data.remote.RealtimeSyncManager
import com.wishify.customer.data.repository.WishifyRepository
import com.wishify.customer.presentation.cart.PriceRow
import com.wishify.customer.presentation.components.EmptyStateView
import com.wishify.customer.presentation.components.ErrorStateView
import com.wishify.customer.presentation.components.LoadingStateView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    repository: WishifyRepository,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var ordersState by remember { mutableStateOf<Result<List<Order>>?>(null) }
    val scope = rememberCoroutineScope()

    val fetchOrders = {
        scope.launch {
            ordersState = repository.getOrders()
        }
    }

    LaunchedEffect(Unit) {
        fetchOrders()
        RealtimeSyncManager.events.collect { eventJson ->
            if (eventJson.contains("ORDER_UPDATED")) {
                fetchOrders()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Orders 📦", fontWeight = FontWeight.Bold) })
        },
        modifier = modifier
    ) { padding ->
        val state = ordersState
        when {
            state == null -> LoadingStateView(Modifier.padding(padding))
            state.isFailure -> ErrorStateView(
                message = state.exceptionOrNull()?.message ?: "Failed to load orders",
                onRetry = { fetchOrders() },
                modifier = Modifier.padding(padding)
            )
            state.isSuccess -> {
                val orders = state.getOrNull()!!
                if (orders.isEmpty()) {
                    EmptyStateView(
                        title = "No orders placed yet",
                        subtitle = "When you order gifts, they will show up here.",
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders) { order ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOrderClick(order.id) }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(order.orderNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        OrderStatusBadge(order.orderStatus)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Recipient: ${order.recipientName}", color = Color.Gray, fontSize = 13.sp)
                                    Text("Delivery Date: ${order.deliveryDate.take(10)}", color = Color.Gray, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${order.items.size} item(s)", fontSize = 13.sp)
                                        Text("Total: ₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    repository: WishifyRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var orderState by remember { mutableStateOf<Result<Order>?>(null) }
    var isCancelling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val fetchOrder = {
        scope.launch {
            orderState = repository.getOrderDetail(orderId)
        }
    }

    LaunchedEffect(orderId) {
        fetchOrder()
        RealtimeSyncManager.events.collect { eventJson ->
            if (eventJson.contains("ORDER_UPDATED")) {
                fetchOrder()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        val state = orderState
        when {
            state == null -> LoadingStateView(Modifier.padding(padding))
            state.isFailure -> ErrorStateView(
                message = state.exceptionOrNull()?.message ?: "Failed to load order",
                onRetry = { fetchOrder() },
                modifier = Modifier.padding(padding)
            )
            state.isSuccess -> {
                val order = state.getOrNull()!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(order.orderNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                    OrderStatusBadge(order.orderStatus)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Payment: ${order.paymentMethod} (${order.paymentStatus})", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    }

                    // Live Status Timeline
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Order Tracking Timeline 📍", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(16.dp))

                                val steps = listOf(
                                    "PLACED" to "Order Placed",
                                    "CONFIRMED" to "Confirmed",
                                    "PREPARED" to "Prepared with Care",
                                    "OUT_FOR_DELIVERY" to "Out for Delivery",
                                    "DELIVERED" to "Delivered"
                                )

                                val currentStepIndex = when (order.orderStatus) {
                                    "PLACED" -> 0
                                    "CONFIRMED" -> 1
                                    "PREPARED" -> 2
                                    "OUT_FOR_DELIVERY" -> 3
                                    "DELIVERED" -> 4
                                    else -> -1
                                }

                                steps.forEachIndexed { index, (key, title) ->
                                    val isCompleted = currentStepIndex >= index && order.orderStatus != "CANCELLED"
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(if (isCompleted) MaterialTheme.colorScheme.primary else Color.LightGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isCompleted) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = title,
                                            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCompleted) MaterialTheme.colorScheme.onSurface else Color.Gray
                                        )
                                    }
                                    if (index < steps.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 11.dp)
                                                .width(2.dp)
                                                .height(24.dp)
                                                .background(if (currentStepIndex > index) MaterialTheme.colorScheme.primary else Color.LightGray)
                                        )
                                    }
                                }

                                if (order.orderStatus == "OUT_FOR_DELIVERY" && order.deliveryPartnerName != null) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Delivery Partner: ${order.deliveryPartnerName}", fontWeight = FontWeight.Bold)
                                            order.deliveryPartnerPhone?.let { Text("Contact: $it", fontSize = 12.sp) }
                                            order.trackingNumber?.let { Text("Tracking No: $it", fontSize = 12.sp) }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Items
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Items Ordered", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(12.dp))
                                order.items.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(item.title, fontWeight = FontWeight.SemiBold)
                                            item.variantTitle?.let { Text("Option: $it", fontSize = 12.sp, color = Color.Gray) }
                                            item.personalizationText?.let { Text("Note: \"$it\"", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary) }
                                            Text("Qty: ${item.quantity}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Text("₹${item.totalPrice.toInt()}", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Price Breakdown
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Payment Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(12.dp))
                                PriceRow("Subtotal", "₹${order.subtotal.toInt()}")
                                PriceRow("Delivery Fee", if (order.deliveryFee == 0.0) "FREE" else "₹${order.deliveryFee.toInt()}")
                                if (order.discountAmount > 0) {
                                    PriceRow("Discount", "-₹${order.discountAmount.toInt()}", Color(0xFF2E7D32))
                                }
                                PriceRow("Taxes", "₹${order.taxAmount.toInt()}")
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                PriceRow("Total Amount", "₹${order.totalAmount.toInt()}")
                            }
                        }
                    }

                    // Cancel Order Button (if eligible)
                    if (order.orderStatus == "PLACED" || order.orderStatus == "CONFIRMED") {
                        item {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isCancelling = true
                                        repository.cancelOrder(order.id, "Customer requested cancellation")
                                        isCancelling = false
                                        fetchOrder()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                enabled = !isCancelling
                            ) {
                                Text(if (isCancelling) "Cancelling..." else "Cancel Order")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "PLACED", "CONFIRMED" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "PREPARED" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "OUT_FOR_DELIVERY" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "DELIVERED" -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        "CANCELLED" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color.LightGray to Color.Black
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
