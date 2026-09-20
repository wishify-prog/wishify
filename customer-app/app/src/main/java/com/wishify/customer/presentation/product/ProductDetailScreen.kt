package com.wishify.customer.presentation.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wishify.customer.data.model.*
import com.wishify.customer.data.repository.WishifyRepository
import com.wishify.customer.presentation.components.ErrorStateView
import com.wishify.customer.presentation.components.LoadingStateView
import com.wishify.customer.presentation.components.ProductCard
import com.wishify.customer.ui.theme.GoldAccent
import com.wishify.customer.ui.theme.RosePrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    slug: String,
    repository: WishifyRepository,
    onBackClick: () -> Unit,
    onAddToCartSuccess: () -> Unit,
    onBuyNowClick: () -> Unit,
    onProductClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var detailState by remember { mutableStateOf<Result<ProductDetailResponse>?>(null) }
    var selectedVariant by remember { mutableStateOf<ProductVariant?>(null) }
    var selectedAddOns by remember { mutableStateOf<Set<String>>(emptySet()) }
    var personalizationText by remember { mutableStateOf("") }
    var isWishlisted by remember { mutableStateOf(false) }
    var pincodeInput by remember { mutableStateOf("560001") }
    var pincodeCheckResult by remember { mutableStateOf<String?>(null) }
    var availableSlots by remember { mutableStateOf<List<DeliverySlot>>(emptyList()) }
    var selectedSlot by remember { mutableStateOf<DeliverySlot?>(null) }
    var isAddingToCart by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val fetchDetail = {
        scope.launch {
            detailState = null
            val result = repository.getProductDetail(slug)
            detailState = result
            if (result.isSuccess) {
                val prod = result.getOrNull()!!.product
                selectedVariant = prod.variants.find { it.isDefault } ?: prod.variants.firstOrNull()
                // Check slots
                val slotsRes = repository.getDeliverySlots(pincodeInput, null)
                if (slotsRes.isSuccess) {
                    availableSlots = slotsRes.getOrNull() ?: emptyList()
                    selectedSlot = availableSlots.firstOrNull { it.isAvailable }
                }
            }
        }
    }

    LaunchedEffect(slug) {
        fetchDetail()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isWishlisted = !isWishlisted }) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) RosePrimary else Color.Gray
                        )
                    }
                }
            )
        },
        bottomBar = {
            detailState?.getOrNull()?.let { detail ->
                val currentPrice = selectedVariant?.price ?: detail.product.basePrice
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Total Price",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "₹${currentPrice.toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isAddingToCart = true
                                    val req = AddToCartRequest(
                                        productId = detail.product.id,
                                        variantId = selectedVariant?.id,
                                        quantity = 1,
                                        personalizationText = if (detail.product.isPersonalized) personalizationText else null,
                                        deliverySlotId = selectedSlot?.id,
                                        addOns = selectedAddOns.map { AddOnRequest(it, 1) }
                                    )
                                    val res = repository.addToCart(req)
                                    isAddingToCart = false
                                    if (res.isSuccess) {
                                        onAddToCartSuccess()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.2f),
                            enabled = !isAddingToCart
                        ) {
                            Text("Add to Cart")
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    val req = AddToCartRequest(
                                        productId = detail.product.id,
                                        variantId = selectedVariant?.id,
                                        quantity = 1,
                                        personalizationText = if (detail.product.isPersonalized) personalizationText else null,
                                        deliverySlotId = selectedSlot?.id,
                                        addOns = selectedAddOns.map { AddOnRequest(it, 1) }
                                    )
                                    repository.addToCart(req)
                                    onBuyNowClick()
                                }
                            },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Buy Now")
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { padding ->
        val state = detailState
        when {
            state == null -> LoadingStateView(Modifier.padding(padding))
            state.isFailure -> ErrorStateView(
                message = state.exceptionOrNull()?.message ?: "Failed to load product",
                onRetry = { fetchDetail() },
                modifier = Modifier.padding(padding)
            )
            state.isSuccess -> {
                val detail = state.getOrNull()!!
                val product = detail.product

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Image Gallery / Carousel
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            val mainImage = product.images.firstOrNull()?.imageUrl ?: ""
                            AsyncImage(
                                model = mainImage,
                                contentDescription = product.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Title & Price Section
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = product.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val displayPrice = selectedVariant?.price ?: product.basePrice
                                Text(
                                    text = "₹${displayPrice.toInt()}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                product.compareAtPrice?.let { comp ->
                                    if (comp > displayPrice) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "₹${comp.toInt()}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.Gray,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        val discountPct = ((comp - displayPrice) / comp * 100).toInt()
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = Color(0xFFE8F5E9),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "$discountPct% OFF",
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Rating & review count
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF2E7D32),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = String.format("%.1f", product.averageRating),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${product.reviewCount} Reviews",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Variants selector
                    if (product.variants.size > 1) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(
                                    text = "Select Option",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(product.variants) { variant ->
                                        val isSelected = selectedVariant?.id == variant.id
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedVariant = variant },
                                            label = {
                                                Text("${variant.title} - ₹${variant.price.toInt()}")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Personalization input if applicable
                    if (product.isPersonalized) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Personalize this Gift ✍️",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = product.personalizationPrompt ?: "Enter custom text to be engraved/printed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = personalizationText,
                                        onValueChange = { personalizationText = it },
                                        placeholder = { Text("e.g. Happy Birthday Rohit!") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    // Delivery Pincode & Slot Picker
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Check Delivery & Select Slot 🚚",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = pincodeInput,
                                        onValueChange = { pincodeInput = it },
                                        label = { Text("Enter 6-digit Pincode") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val res = repository.checkPincode(pincodeInput)
                                                if (res.isSuccess) {
                                                    pincodeCheckResult = res.getOrNull()?.message
                                                    val slotsRes = repository.getDeliverySlots(pincodeInput, null)
                                                    if (slotsRes.isSuccess) {
                                                        availableSlots = slotsRes.getOrNull() ?: emptyList()
                                                    }
                                                } else {
                                                    pincodeCheckResult = "Delivery not available"
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Check")
                                    }
                                }

                                pincodeCheckResult?.let { msg ->
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = msg,
                                        color = if (msg.contains("available", ignoreCase = true)) Color(0xFF2E7D32) else Color.Red,
                                        fontSize = 12.sp
                                    )
                                }

                                if (availableSlots.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Available Delivery Slots:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    availableSlots.forEach { slot ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(enabled = slot.isAvailable) {
                                                    selectedSlot = slot
                                                }
                                                .padding(vertical = 4.dp)
                                        ) {
                                            RadioButton(
                                                selected = selectedSlot?.id == slot.id,
                                                onClick = { selectedSlot = slot },
                                                enabled = slot.isAvailable
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "${slot.title} - ${if (slot.fee == 0.0) "Free" else "₹" + slot.fee.toInt()}",
                                                    fontWeight = if (selectedSlot?.id == slot.id) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (slot.isAvailable) Color.Unspecified else Color.Gray
                                                )
                                                if (!slot.isAvailable && slot.reason != null) {
                                                    Text(
                                                        text = slot.reason,
                                                        fontSize = 10.sp,
                                                        color = Color.Red
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add-Ons Checkboxes
                    if (product.addOns.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = "Make it Extra Special (Add-ons)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                product.addOns.forEach { addOn ->
                                    val isChecked = selectedAddOns.contains(addOn.id)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                selectedAddOns = if (isChecked) {
                                                    selectedAddOns - addOn.id
                                                } else {
                                                    selectedAddOns + addOn.id
                                                }
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    selectedAddOns = if (checked) selectedAddOns + addOn.id else selectedAddOns - addOn.id
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(addOn.title, fontWeight = FontWeight.Medium)
                                                Text("₹${addOn.price.toInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Description
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = product.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Similar Products
                    if (detail.similarProducts.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "You May Also Like",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(detail.similarProducts) { similar ->
                                    ProductCard(
                                        product = similar,
                                        onClick = { onProductClick(similar.slug) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}
