package com.wishify.customer.presentation.checkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wishify.customer.data.model.*
import com.wishify.customer.data.repository.WishifyRepository
import com.wishify.customer.presentation.components.LoadingStateView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    repository: WishifyRepository,
    onBackClick: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var addressesState by remember { mutableStateOf<List<Address>>(emptyList()) }
    var selectedAddressId by remember { mutableStateOf<String?>(null) }
    var slotsState by remember { mutableStateOf<List<DeliverySlot>>(emptyList()) }
    var selectedSlotId by remember { mutableStateOf<String?>(null) }
    
    var recipientName by remember { mutableStateOf("Priya Sharma") }
    var recipientPhone by remember { mutableStateOf("+919876543210") }
    var giftMessage by remember { mutableStateOf("Wishing you joy, love, and happiness!") }
    var isSenderHidden by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("RAZORPAY") }
    var isPlacingOrder by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    LaunchedEffect(Unit) {
        val addrs = repository.getAddresses()
        if (addrs.isSuccess) {
            val list = addrs.getOrNull() ?: emptyList()
            addressesState = list
            selectedAddressId = list.find { it.isDefault }?.id ?: list.firstOrNull()?.id
        }

        val slots = repository.getDeliverySlots("560001", todayStr)
        if (slots.isSuccess) {
            val list = slots.getOrNull() ?: emptyList()
            slotsState = list
            selectedSlotId = list.firstOrNull { it.isAvailable }?.id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout & Delivery 🚚", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedAddressId == null) {
                                errorMessage = "Please select a delivery address"
                                return@Button
                            }
                            if (selectedSlotId == null) {
                                errorMessage = "Please select a delivery slot"
                                return@Button
                            }

                            scope.launch {
                                isPlacingOrder = true
                                errorMessage = null

                                val req = InitiateCheckoutRequest(
                                    addressId = selectedAddressId!!,
                                    paymentMethod = selectedPaymentMethod,
                                    recipientName = recipientName,
                                    recipientPhone = recipientPhone,
                                    giftMessage = giftMessage,
                                    isSenderHidden = isSenderHidden,
                                    deliveryDate = todayStr,
                                    deliverySlotId = selectedSlotId!!
                                )

                                val res = repository.initiateCheckout(req)
                                if (res.isSuccess) {
                                    val checkoutData = res.getOrNull()!!
                                    val order = checkoutData.order

                                    if (selectedPaymentMethod == "RAZORPAY" && checkoutData.razorpay != null) {
                                        // Simulate successful Razorpay SDK payment callback
                                        val verifyReq = VerifyPaymentRequest(
                                            orderId = order.id,
                                            razorpayOrderId = checkoutData.razorpay.orderId,
                                            razorpayPaymentId = "pay_mock_${Date().time}",
                                            razorpaySignature = "mock_sig_valid"
                                        )
                                        repository.verifyPayment(verifyReq)
                                    }

                                    isPlacingOrder = false
                                    onOrderPlaced(order.id)
                                } else {
                                    isPlacingOrder = false
                                    errorMessage = res.exceptionOrNull()?.message ?: "Failed to place order"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isPlacingOrder
                    ) {
                        if (isPlacingOrder) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (selectedPaymentMethod == "RAZORPAY") "Pay & Place Order" else "Confirm COD Order",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Recipient Details
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Recipient Details 👤", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = { recipientName = it },
                            label = { Text("Recipient Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = recipientPhone,
                            onValueChange = { recipientPhone = it },
                            label = { Text("Recipient Phone") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = giftMessage,
                            onValueChange = { giftMessage = it },
                            label = { Text("Gift Card Message (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Hide Sender Name", fontWeight = FontWeight.Medium)
                                Text("Send anonymously as a surprise", fontSize = 12.sp, color = Color.Gray)
                            }
                            Switch(
                                checked = isSenderHidden,
                                onCheckedChange = { isSenderHidden = it }
                            )
                        }
                    }
                }
            }

            // 2. Delivery Address
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Delivery Address 📍", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (addressesState.isEmpty()) {
                            Text("No saved address. Using default Indiranagar address.", color = Color.Gray, fontSize = 13.sp)
                        } else {
                            addressesState.forEach { addr ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAddressId = addr.id }
                                        .padding(vertical = 6.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedAddressId == addr.id,
                                        onClick = { selectedAddressId = addr.id }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(addr.name, fontWeight = FontWeight.SemiBold)
                                        Text("${addr.addressLine1}, ${addr.city} - ${addr.pincode}", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Delivery Slot
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Delivery Slot ⏰", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        slotsState.forEach { slot ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = slot.isAvailable) { selectedSlotId = slot.id }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedSlotId == slot.id,
                                    onClick = { selectedSlotId = slot.id },
                                    enabled = slot.isAvailable
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${slot.title} (${if (slot.fee == 0.0) "Free" else "₹" + slot.fee.toInt()})",
                                    fontWeight = if (selectedSlotId == slot.id) FontWeight.Bold else FontWeight.Normal,
                                    color = if (slot.isAvailable) Color.Unspecified else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // 4. Payment Method
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Payment Method 💳", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPaymentMethod = "RAZORPAY" }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = selectedPaymentMethod == "RAZORPAY",
                                onClick = { selectedPaymentMethod = "RAZORPAY" }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Online Payment (Razorpay)", fontWeight = FontWeight.SemiBold)
                                Text("UPI, Google Pay, PhonePe, Cards, NetBanking", color = Color.Gray, fontSize = 12.sp)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPaymentMethod = "COD" }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = selectedPaymentMethod == "COD",
                                onClick = { selectedPaymentMethod = "COD" }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Cash on Delivery (COD)", fontWeight = FontWeight.SemiBold)
                                Text("Pay with cash or UPI at delivery doorstep", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
