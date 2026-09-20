package com.wishify.admin.presentation.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.wishify.admin.data.model.*
import com.wishify.admin.data.repository.WishifyAdminRepository
import com.wishify.admin.ui.theme.GoldAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCatalogScreen(
    repository: WishifyAdminRepository,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Banners", "Categories", "Occasions", "Coupons", "Slots", "Pincodes")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catalog & System Management ⚙️", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> BannersTab(repository)
                1 -> CategoriesTab(repository)
                2 -> OccasionsTab(repository)
                3 -> CouponsTab(repository)
                4 -> SlotsTab(repository)
                5 -> PincodesTab(repository)
            }
        }
    }
}

// ==========================================
// --- BANNERS TAB ---
// ==========================================
@Composable
fun BannersTab(repository: WishifyAdminRepository) {
    var banners by remember { mutableStateOf<List<AdminBanner>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val refresh = {
        scope.launch {
            isLoading = true
            repository.getBanners().onSuccess { banners = it }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldAccent)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(banners) { banner ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = banner.imageUrl,
                                contentDescription = banner.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(banner.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    banner.subtitle?.let { Text(it, color = Color.Gray, fontSize = 13.sp) }
                                }
                                Row {
                                    IconButton(onClick = {
                                        editingId = banner.id
                                        title = banner.title
                                        subtitle = banner.subtitle ?: ""
                                        imageUrl = banner.imageUrl
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = GoldAccent)
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            repository.deleteBanner(banner.id)
                                            refresh()
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingId = null
                title = ""
                subtitle = ""
                imageUrl = "https://images.unsplash.com/photo-1582794543139-8ac9cb0f7b11?auto=format&fit=crop&w=800&q=80"
                showDialog = true
            },
            containerColor = GoldAccent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Banner", tint = Color.White)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingId == null) "Add Banner" else "Edit Banner") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Subtitle (Optional)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val req = CreateBannerRequest(title = title, subtitle = subtitle.ifBlank { null }, imageUrl = imageUrl)
                            if (editingId == null) {
                                repository.createBanner(req)
                            } else {
                                repository.updateBanner(editingId!!, req)
                            }
                            showDialog = false
                            refresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// --- CATEGORIES TAB ---
// ==========================================
@Composable
fun CategoriesTab(repository: WishifyAdminRepository) {
    var categories by remember { mutableStateOf<List<AdminCategory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val refresh = {
        scope.launch {
            isLoading = true
            repository.getCategories().onSuccess { categories = it }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldAccent)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories) { category ->
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
                                model = category.imageUrl ?: "",
                                contentDescription = category.name,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("slug: /${category.slug}", color = Color.Gray, fontSize = 12.sp)
                            }
                            IconButton(onClick = {
                                editingId = category.id
                                name = category.name
                                slug = category.slug
                                imageUrl = category.imageUrl ?: ""
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = GoldAccent)
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    repository.deleteCategory(category.id)
                                    refresh()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingId = null
                name = ""
                slug = ""
                imageUrl = "https://images.unsplash.com/photo-1563245372-f21724e3856d?auto=format&fit=crop&w=400&q=80"
                showDialog = true
            },
            containerColor = GoldAccent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Category", tint = Color.White)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingId == null) "Add Category" else "Edit Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = {
                        name = it
                        if (editingId == null) slug = it.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")
                    }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = slug, onValueChange = { slug = it }, label = { Text("Slug") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val req = CreateCategoryRequest(name = name, slug = slug, imageUrl = imageUrl.ifBlank { null })
                            if (editingId == null) {
                                repository.createCategory(req)
                            } else {
                                repository.updateCategory(editingId!!, req)
                            }
                            showDialog = false
                            refresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// --- OCCASIONS TAB ---
// ==========================================
@Composable
fun OccasionsTab(repository: WishifyAdminRepository) {
    var occasions by remember { mutableStateOf<List<AdminOccasion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val refresh = {
        scope.launch {
            isLoading = true
            repository.getOccasions().onSuccess { occasions = it }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldAccent)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(occasions) { occasion ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(occasion.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("slug: /${occasion.slug}", color = Color.Gray, fontSize = 12.sp)
                            }
                            Row {
                                IconButton(onClick = {
                                    editingId = occasion.id
                                    name = occasion.name
                                    slug = occasion.slug
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = GoldAccent)
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        repository.deleteOccasion(occasion.id)
                                        refresh()
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingId = null
                name = ""
                slug = ""
                showDialog = true
            },
            containerColor = GoldAccent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Occasion", tint = Color.White)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingId == null) "Add Occasion" else "Edit Occasion") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = {
                        name = it
                        if (editingId == null) slug = it.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")
                    }, label = { Text("Occasion Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = slug, onValueChange = { slug = it }, label = { Text("Slug") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val req = CreateOccasionRequest(name = name, slug = slug)
                            if (editingId == null) {
                                repository.createOccasion(req)
                            } else {
                                repository.updateOccasion(editingId!!, req)
                            }
                            showDialog = false
                            refresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// --- COUPONS TAB ---
// ==========================================
@Composable
fun CouponsTab(repository: WishifyAdminRepository) {
    var coupons by remember { mutableStateOf<List<AdminCoupon>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var discountValue by remember { mutableStateOf("15") }
    var discountType by remember { mutableStateOf("PERCENTAGE") }
    var minOrderValue by remember { mutableStateOf("499") }
    var validTo by remember { mutableStateOf("2028-12-31T23:59:59Z") }
    var editingId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val refresh = {
        scope.launch {
            isLoading = true
            repository.getCoupons().onSuccess { coupons = it }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldAccent)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(coupons) { coupon ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(coupon.code, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GoldAccent)
                                Text(
                                    text = if (coupon.discountType == "PERCENTAGE") "${coupon.discountValue.toInt()}% OFF (Min ₹${coupon.minOrderValue.toInt()})"
                                    else "₹${coupon.discountValue.toInt()} FLAT OFF (Min ₹${coupon.minOrderValue.toInt()})",
                                    fontSize = 13.sp
                                )
                                Text("Used ${coupon.usedCount} times", color = Color.Gray, fontSize = 12.sp)
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    repository.deleteCoupon(coupon.id)
                                    refresh()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingId = null
                code = "FESTIVE20"
                discountValue = "20"
                minOrderValue = "599"
                showDialog = true
            },
            containerColor = GoldAccent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Coupon", tint = Color.White)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Create Coupon") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Coupon Code") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = discountValue, onValueChange = { discountValue = it }, label = { Text("Discount Value (% or ₹)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = minOrderValue, onValueChange = { minOrderValue = it }, label = { Text("Min Order Value (₹)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val req = CreateCouponRequest(
                                code = code,
                                discountType = discountType,
                                discountValue = discountValue.toDoubleOrNull() ?: 10.0,
                                minOrderValue = minOrderValue.toDoubleOrNull() ?: 0.0,
                                validTo = validTo
                            )
                            repository.createCoupon(req)
                            showDialog = false
                            refresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// --- SLOTS TAB ---
// ==========================================
@Composable
fun SlotsTab(repository: WishifyAdminRepository) {
    var slots by remember { mutableStateOf<List<AdminSlot>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var slotType by remember { mutableStateOf("STANDARD") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("13:00") }
    var fee by remember { mutableStateOf("0") }
    val scope = rememberCoroutineScope()

    val refresh = {
        scope.launch {
            isLoading = true
            repository.getSlots().onSuccess { slots = it }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldAccent)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(slots) { slot ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(slot.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${slot.startTime} - ${slot.endTime} (${slot.slotType})", color = Color.Gray, fontSize = 13.sp)
                                Text("Delivery Fee: ₹${slot.fee.toInt()} | Cutoff: ${slot.cutoffHoursBefore}h before", fontSize = 12.sp, color = GoldAccent)
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    repository.deleteSlot(slot.id)
                                    refresh()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                title = "Evening Express (17:00 - 20:00)"
                slotType = "STANDARD"
                startTime = "17:00"
                endTime = "20:00"
                fee = "49"
                showDialog = true
            },
            containerColor = GoldAccent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Slot", tint = Color.White)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Delivery Slot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Slot Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start Time (HH:mm)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End Time (HH:mm)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = fee, onValueChange = { fee = it }, label = { Text("Fee (₹)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val req = CreateSlotRequest(
                                title = title,
                                slotType = slotType,
                                startTime = startTime,
                                endTime = endTime,
                                fee = fee.toDoubleOrNull() ?: 0.0
                            )
                            repository.createSlot(req)
                            showDialog = false
                            refresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// --- PINCODES TAB ---
// ==========================================
@Composable
fun PincodesTab(repository: WishifyAdminRepository) {
    var pincodes by remember { mutableStateOf<List<AdminPincode>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val refresh = {
        scope.launch {
            isLoading = true
            repository.getPincodes().onSuccess { pincodes = it }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = GoldAccent)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(pincodes) { pin ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pin.code, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GoldAccent)
                                Text("${pin.city}, ${pin.state}", color = Color.Gray, fontSize = 13.sp)
                                Text("Same-Day: ${if (pin.isSameDayAvailable) "✓" else "✗"} | Midnight: ${if (pin.isMidnightAvailable) "✓" else "✗"}", fontSize = 12.sp)
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    repository.deletePincode(pin.id)
                                    refresh()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                code = "110001"
                city = "New Delhi"
                state = "Delhi"
                showDialog = true
            },
            containerColor = GoldAccent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Pincode", tint = Color.White)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add / Update Pincode") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Pincode (6 digits)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val req = CreatePincodeRequest(
                                code = code,
                                city = city,
                                state = state
                            )
                            repository.createPincode(req)
                            showDialog = false
                            refresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}
