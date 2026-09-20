package com.wishify.customer.presentation.checkout

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wishify.customer.data.local.UserPreferences
import com.wishify.customer.data.model.*
import com.wishify.customer.data.repository.WishifyRepository
import com.wishify.customer.ui.theme.GoldAccent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    repository: WishifyRepository,
    userPreferences: UserPreferences,
    onBackClick: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val defaultFallbackAddress = remember {
        Address(
            id = "default-indiranagar",
            name = "Priya Sharma",
            phone = "+919876543210",
            addressLine1 = "Flat 402, Lotus Heights, Indiranagar 100ft Road",
            city = "Bengaluru",
            state = "Karnataka",
            pincode = "560001",
            isDefault = true
        )
    }

    var addressesState by remember { mutableStateOf<List<Address>>(listOf(defaultFallbackAddress)) }
    var selectedAddressId by remember { mutableStateOf<String?>(defaultFallbackAddress.id) }
    var slotsState by remember { mutableStateOf<List<DeliverySlot>>(emptyList()) }
    var selectedSlotId by remember { mutableStateOf<String?>(null) }
    var cartState by remember { mutableStateOf<CartResponse?>(null) }
    var paymentSettings by remember { mutableStateOf<PaymentSetting?>(null) }

    var recipientName by remember { mutableStateOf("Priya Sharma") }
    var recipientPhone by remember { mutableStateOf("+919876543210") }
    var giftMessage by remember { mutableStateOf("Wishing you joy, love, and happiness!") }
    var isSenderHidden by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("UPI_QR") }
    var isPlacingOrder by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Delivery Address Dialog State
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var newAddressName by remember { mutableStateOf("") }
    var newAddressPhone by remember { mutableStateOf("") }
    var newAddressLine1 by remember { mutableStateOf("") }
    var newAddressCity by remember { mutableStateOf("Bengaluru") }
    var newAddressState by remember { mutableStateOf("Karnataka") }
    var newAddressPincode by remember { mutableStateOf("560001") }
    var isSavingAddress by remember { mutableStateOf(false) }

    // UPI QR Popup Modal Dialog State
    var showUpiQrDialog by remember { mutableStateOf(false) }
    var upiTransactionRef by remember { mutableStateOf("") }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    LaunchedEffect(Unit) {
        val localList = userPreferences.getSavedAddressesDirect()
        if (localList.isNotEmpty()) {
            addressesState = localList
            selectedAddressId = localList.find { it.isDefault }?.id ?: localList.first().id
            val active = localList.find { it.id == selectedAddressId } ?: localList.first()
            recipientName = active.name
            recipientPhone = active.phone
        }

        val addrs = repository.getAddresses()
        if (addrs.isSuccess) {
            val list = addrs.getOrNull() ?: emptyList()
            if (list.isNotEmpty()) {
                addressesState = list
                selectedAddressId = list.find { it.isDefault }?.id ?: list.first().id
                val active = list.find { it.id == selectedAddressId } ?: list.first()
                recipientName = active.name
                recipientPhone = active.phone
                userPreferences.saveSavedAddresses(list)
            } else if (localList.isEmpty()) {
                addressesState = listOf(defaultFallbackAddress)
                selectedAddressId = defaultFallbackAddress.id
            }
        } else if (localList.isEmpty()) {
            addressesState = listOf(defaultFallbackAddress)
            selectedAddressId = defaultFallbackAddress.id
        }

        val slots = repository.getDeliverySlots("560001", todayStr)
        if (slots.isSuccess) {
            val list = slots.getOrNull() ?: emptyList()
            slotsState = list
            selectedSlotId = list.firstOrNull { it.isAvailable }?.id
        }

        val cartRes = repository.getCart()
        if (cartRes.isSuccess) {
            cartState = cartRes.getOrNull()
        }

        val settingsRes = repository.getPaymentSettings()
        if (settingsRes.isSuccess) {
            paymentSettings = settingsRes.getOrNull()
        }
    }

    // Pricing calculation
    val subtotal = cartState?.subtotal ?: 0.0
    val selectedSlotFee = slotsState.find { it.id == selectedSlotId }?.fee ?: (cartState?.deliveryFee ?: 0.0)
    val taxAmount = cartState?.taxAmount ?: 0.0
    val totalAmount = if (cartState != null) {
        maxOf(0.0, subtotal + selectedSlotFee + taxAmount)
    } else {
        0.0
    }

    // COD Rule: Disable COD if order > ₹100
    val codLimit = paymentSettings?.codMaxAmount ?: 100.0
    val isCodDisabled = totalAmount > codLimit

    LaunchedEffect(isCodDisabled) {
        if (isCodDisabled && selectedPaymentMethod == "COD") {
            selectedPaymentMethod = "UPI_QR"
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

                            if (selectedPaymentMethod == "UPI_QR") {
                                // Popup UPI QR image for online payment
                                showUpiQrDialog = true
                                return@Button
                            }

                            // Otherwise, place COD order
                            scope.launch {
                                isPlacingOrder = true
                                errorMessage = null

                                val req = InitiateCheckoutRequest(
                                    addressId = selectedAddressId!!,
                                    paymentMethod = "COD",
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
                                    isPlacingOrder = false
                                    onOrderPlaced(checkoutData.order.id)
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
                                text = if (selectedPaymentMethod == "UPI_QR") {
                                    "Pay Online with UPI QR (₹${totalAmount.toInt()})"
                                } else {
                                    "Confirm COD Order (₹${totalAmount.toInt()})"
                                },
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Delivery Address 📍", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            TextButton(
                                onClick = {
                                    newAddressName = recipientName
                                    newAddressPhone = recipientPhone
                                    showAddAddressDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add New", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            addressesState.forEach { addr ->
                                val isSelected = selectedAddressId == addr.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                    border = if (isSelected) {
                                        androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    } else {
                                        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedAddressId = addr.id
                                            recipientName = addr.name
                                            recipientPhone = addr.phone
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                selectedAddressId = addr.id
                                                recipientName = addr.name
                                                recipientPhone = addr.phone
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(addr.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                if (addr.isDefault) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                    ) {
                                                        Text(
                                                            "DEFAULT",
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(addr.phone, fontSize = 12.sp, color = Color.Gray)
                                            Text(
                                                "${addr.addressLine1}, ${addr.city} - ${addr.pincode}",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                        }
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

            // 4. Payment Method (Conditional COD & Pay Online)
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Payment Method 💳", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            if (isCodDisabled) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = "Online Payment Required",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // OPTION 1: Pay Online (UPI QR)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedPaymentMethod == "UPI_QR") {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            } else {
                                Color.Transparent
                            },
                            border = if (selectedPaymentMethod == "UPI_QR") {
                                androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPaymentMethod = "UPI_QR"
                                    showUpiQrDialog = true
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == "UPI_QR",
                                    onClick = {
                                        selectedPaymentMethod = "UPI_QR"
                                        showUpiQrDialog = true
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Pay Online (UPI QR Code)", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF2E7D32).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                "⚡ Instant",
                                                color = Color(0xFF2E7D32),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "Scan QR & pay using Google Pay, PhonePe, Paytm, BHIM",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                                Icon(
                                    Icons.Default.QrCode2,
                                    contentDescription = "Scan QR",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // OPTION 2: Cash on Delivery (COD)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedPaymentMethod == "COD") {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            } else {
                                Color.Transparent
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isCodDisabled) {
                                    selectedPaymentMethod = "COD"
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = selectedPaymentMethod == "COD",
                                        onClick = { selectedPaymentMethod = "COD" },
                                        enabled = !isCodDisabled
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Cash on Delivery (COD)",
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isCodDisabled) Color.Gray else Color.Unspecified
                                        )
                                        Text(
                                            text = "Pay with cash at delivery doorstep",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Warning if COD is disabled
                                if (isCodDisabled) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "COD is disabled for orders above ₹${codLimit.toInt()}. Please pay online.",
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Order Summary
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Order Summary 📋", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal (${cartState?.itemCount ?: 0} items)", color = Color.Gray, fontSize = 13.sp)
                            Text("₹${subtotal.toInt()}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery Slot Fee", color = Color.Gray, fontSize = 13.sp)
                            Text(if (selectedSlotFee == 0.0) "FREE" else "₹${selectedSlotFee.toInt()}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Taxes (GST 5%)", color = Color.Gray, fontSize = 13.sp)
                            Text("₹${taxAmount.toInt()}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Payable", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("₹${totalAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // --- UPI QR POPUP MODAL DIALOG ---
    if (showUpiQrDialog) {
        val upiIdText = paymentSettings?.upiId ?: "wishify@upi"
        val qrUrl = paymentSettings?.qrImageUrl?.ifBlank { null }
            ?: "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=upi://pay?pa=$upiIdText&pn=Wishify&cu=INR"

        AlertDialog(
            onDismissRequest = {
                if (!isPlacingOrder) showUpiQrDialog = false
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scan UPI QR to Pay", fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { if (!isPlacingOrder) showUpiQrDialog = false },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Payable Amount Highlight
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Amount to Pay", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "₹${totalAmount.toInt()}",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // QR Code Image
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(2.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = qrUrl,
                            contentDescription = "Scan UPI QR Code",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // UPI ID & Beneficiary with Copy Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = paymentSettings?.accountHolderName ?: "Wishify Gifts",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = upiIdText,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(upiIdText))
                                    Toast.makeText(context, "UPI ID copied!", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 12.sp)
                            }
                        }
                    }

                    // Guidance Steps
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("Instructions:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("1. Open any UPI App (Google Pay, PhonePe, Paytm).", fontSize = 11.sp, color = Color.Gray)
                        Text("2. Scan this QR code and pay ₹${totalAmount.toInt()}.", fontSize = 11.sp, color = Color.Gray)
                        Text("3. Enter the UTR / Ref ID below and confirm.", fontSize = 11.sp, color = Color.Gray)
                    }

                    // Optional UTR Number Input
                    OutlinedTextField(
                        value = upiTransactionRef,
                        onValueChange = { upiTransactionRef = it },
                        label = { Text("UPI Ref / UTR Number (Optional)") },
                        placeholder = { Text("e.g. 12-digit UTR") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isPlacingOrder = true
                            errorMessage = null

                            val req = InitiateCheckoutRequest(
                                addressId = selectedAddressId!!,
                                paymentMethod = "UPI_QR",
                                recipientName = recipientName,
                                recipientPhone = recipientPhone,
                                giftMessage = giftMessage,
                                isSenderHidden = isSenderHidden,
                                deliveryDate = todayStr,
                                deliverySlotId = selectedSlotId!!,
                                transactionReference = upiTransactionRef.ifBlank { null }
                            )

                            val res = repository.initiateCheckout(req)
                            if (res.isSuccess) {
                                val checkoutData = res.getOrNull()!!
                                showUpiQrDialog = false
                                isPlacingOrder = false
                                onOrderPlaced(checkoutData.order.id)
                            } else {
                                isPlacingOrder = false
                                errorMessage = res.exceptionOrNull()?.message ?: "Failed to place order"
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPlacingOrder
                ) {
                    if (isPlacingOrder) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("I Have Paid - Place Order", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isPlacingOrder) showUpiQrDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- ADD ADDRESS MODAL DIALOG ---
    if (showAddAddressDialog) {
        var addAddressError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = {
                if (!isSavingAddress) showAddAddressDialog = false
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add Delivery Address 📍", fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { if (!isSavingAddress) showAddAddressDialog = false },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    addAddressError?.let {
                        Text(it, color = Color.Red, fontSize = 12.sp)
                    }

                    OutlinedTextField(
                        value = newAddressName,
                        onValueChange = { newAddressName = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newAddressPhone,
                        onValueChange = { newAddressPhone = it },
                        label = { Text("Phone Number *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newAddressLine1,
                        onValueChange = { newAddressLine1 = it },
                        label = { Text("Flat / House No. / Street Address *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newAddressCity,
                            onValueChange = { newAddressCity = it },
                            label = { Text("City *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newAddressPincode,
                            onValueChange = { newAddressPincode = it },
                            label = { Text("Pincode *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = newAddressState,
                        onValueChange = { newAddressState = it },
                        label = { Text("State *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAddressName.isBlank() || newAddressPhone.isBlank() || newAddressLine1.isBlank() || newAddressPincode.isBlank()) {
                            addAddressError = "Please fill in all required fields (*)"
                            return@Button
                        }

                        scope.launch {
                            isSavingAddress = true
                            addAddressError = null

                            val newAddr = Address(
                                name = newAddressName.trim(),
                                phone = newAddressPhone.trim(),
                                addressLine1 = newAddressLine1.trim(),
                                city = newAddressCity.trim().ifBlank { "Bengaluru" },
                                state = newAddressState.trim().ifBlank { "Karnataka" },
                                pincode = newAddressPincode.trim(),
                                isDefault = true
                            )

                            val res = repository.addAddress(newAddr)
                            val saved = if (res.isSuccess) {
                                res.getOrNull() ?: newAddr
                            } else {
                                newAddr.copy(id = "local-" + System.currentTimeMillis())
                            }

                            val updatedList = listOf(saved) + addressesState.filter { it.id != saved.id && it.id != "default-indiranagar" }
                            addressesState = updatedList
                            selectedAddressId = saved.id
                            recipientName = saved.name
                            recipientPhone = saved.phone

                            // Persist immediately in local storage so it is never lost
                            userPreferences.saveSavedAddresses(updatedList)

                            isSavingAddress = false
                            showAddAddressDialog = false
                            Toast.makeText(context, "Address saved successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isSavingAddress
                ) {
                    if (isSavingAddress) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Save & Deliver Here", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isSavingAddress) showAddAddressDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

