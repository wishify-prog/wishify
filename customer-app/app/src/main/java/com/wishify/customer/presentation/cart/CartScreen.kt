package com.wishify.customer.presentation.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wishify.customer.data.model.CartResponse
import com.wishify.customer.data.repository.WishifyRepository
import com.wishify.customer.presentation.components.EmptyStateView
import com.wishify.customer.presentation.components.ErrorStateView
import com.wishify.customer.presentation.components.LoadingStateView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    repository: WishifyRepository,
    onProceedToCheckout: () -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cartState by remember { mutableStateOf<Result<CartResponse>?>(null) }
    var couponInput by remember { mutableStateOf("") }
    var couponDiscount by remember { mutableStateOf<Double?>(null) }
    var couponMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val fetchCart = {
        scope.launch {
            cartState = null
            cartState = repository.getCart()
        }
    }

    LaunchedEffect(Unit) {
        fetchCart()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Gift Cart 🎁", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            cartState?.getOrNull()?.let { cart ->
                if (cart.items.isNotEmpty()) {
                    val discount = couponDiscount ?: 0.0
                    val finalTotal = maxOf(0.0, cart.totalAmount - discount)

                    Surface(
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Payable",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "₹${finalTotal.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Button(
                                onClick = onProceedToCheckout,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Proceed to Checkout", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { padding ->
        val state = cartState
        when {
            state == null -> LoadingStateView(Modifier.padding(padding))
            state.isFailure -> ErrorStateView(
                message = state.exceptionOrNull()?.message ?: "Failed to load cart",
                onRetry = { fetchCart() },
                modifier = Modifier.padding(padding)
            )
            state.isSuccess -> {
                val cart = state.getOrNull()!!
                if (cart.items.isEmpty()) {
                    EmptyStateView(
                        title = "Your cart is empty",
                        subtitle = "Find something special for someone you love!",
                        actionTitle = "Start Shopping",
                        onAction = onContinueShopping,
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Cart Items
                        items(cart.items) { item ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = item.productImage ?: "",
                                        contentDescription = item.productTitle,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.productTitle,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        item.variantTitle?.let {
                                            Text(
                                                text = "Option: $it",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        if (item.addOns.isNotEmpty()) {
                                            Text(
                                                text = "+ ${item.addOns.size} Add-on(s)",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "₹${item.totalItemPrice.toInt()}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                repository.removeCartItem(item.id)
                                                fetchCart()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        // Coupon Section
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Apply Coupon Code",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = couponInput,
                                            onValueChange = { couponInput = it.uppercase() },
                                            placeholder = { Text("e.g. FIRSTGIFT") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    val res = repository.validateCoupon(couponInput, cart.subtotal)
                                                    if (res.isSuccess) {
                                                        val data = res.getOrNull()!!
                                                        couponDiscount = data.discountAmount
                                                        couponMessage = "Coupon applied! Saved ₹${data.discountAmount.toInt()}"
                                                    } else {
                                                        couponDiscount = null
                                                        couponMessage = res.exceptionOrNull()?.message ?: "Invalid coupon"
                                                    }
                                                }
                                            }
                                        ) {
                                            Text("Apply")
                                        }
                                    }
                                    couponMessage?.let { msg ->
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = msg,
                                            color = if (couponDiscount != null) Color(0xFF2E7D32) else Color.Red,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Price Summary
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Price Details",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    PriceRow("Subtotal", "₹${cart.subtotal.toInt()}")
                                    PriceRow("Delivery Fee", if (cart.deliveryFee == 0.0) "FREE" else "₹${cart.deliveryFee.toInt()}")
                                    couponDiscount?.let {
                                        PriceRow("Coupon Discount", "-₹${it.toInt()}", Color(0xFF2E7D32))
                                    }
                                    PriceRow("Estimated Taxes (GST 5%)", "₹${cart.taxAmount.toInt()}")

                                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                                    val finalTotal = maxOf(0.0, cart.totalAmount - (couponDiscount ?: 0.0))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("₹${finalTotal.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
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

@Composable
fun PriceRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, fontWeight = FontWeight.SemiBold, color = valueColor, style = MaterialTheme.typography.bodyMedium)
    }
}
