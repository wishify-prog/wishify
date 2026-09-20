package com.wishify.admin.presentation.products

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
fun AdminProductsScreen(
    repository: WishifyAdminRepository,
    modifier: Modifier = Modifier
) {
    var searchInput by remember { mutableStateOf("") }
    var productsState by remember { mutableStateOf<Result<List<AdminProduct>>?>(null) }
    var categories by remember { mutableStateOf<List<AdminCategory>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // Dialog States
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<AdminProduct?>(null) }

    val fetchProducts = {
        scope.launch {
            productsState = null
            productsState = repository.getProducts(searchInput.ifBlank { null })
        }
    }

    LaunchedEffect(Unit) {
        fetchProducts()
        repository.getCategories().onSuccess { categories = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Inventory 📦", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { fetchProducts() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GoldAccent)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = GoldAccent
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product", tint = Color.Black)
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = {
                    searchInput = it
                    fetchProducts()
                },
                placeholder = { Text("Search products by title or slug...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            val state = productsState
            when {
                state == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldAccent)
                    }
                }
                state.isFailure -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Failed to load products: ${state.exceptionOrNull()?.message}", color = Color.Red)
                    }
                }
                state.isSuccess -> {
                    val products = state.getOrNull()!!
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(products) { product ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editingProduct = product }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val img = product.images.firstOrNull()?.imageUrl ?: ""
                                    AsyncImage(
                                        model = img,
                                        contentDescription = product.title,
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(product.title, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text("₹${product.basePrice.toInt()}", color = GoldAccent, fontWeight = FontWeight.SemiBold)
                                        val totalStock = product.variants.sumOf { it.stockQuantity }
                                        Text(
                                            text = "Stock: $totalStock units (${product.variants.size} variants)",
                                            fontSize = 12.sp,
                                            color = if (totalStock <= 15) Color(0xFFE57373) else Color.Gray
                                        )
                                    }
                                    IconButton(onClick = { editingProduct = product }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = GoldAccent)
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            repository.deleteProduct(product.id, permanent = true)
                                            fetchProducts()
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                    Switch(
                                        checked = product.isActive,
                                        onCheckedChange = {
                                            scope.launch {
                                                repository.toggleProductActive(product.id)
                                                fetchProducts()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- CREATE PRODUCT DIALOG ---
    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var slug by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("Freshly crafted with the finest ingredients and delivered with care.") }
        var price by remember { mutableStateOf("699") }
        var imageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1582794543139-8ac9cb0f7b11?auto=format&fit=crop&w=600&q=80") }
        var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Add New Product") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            slug = it.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")
                        },
                        label = { Text("Product Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = slug,
                        onValueChange = { slug = it },
                        label = { Text("URL Slug") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Base Price (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val catId = if (selectedCategoryId.isNotBlank()) selectedCategoryId
                            else categories.firstOrNull()?.id ?: ""

                            if (catId.isNotBlank() && title.isNotBlank()) {
                                val req = CreateProductRequest(
                                    title = title,
                                    slug = slug.ifBlank { "item-${System.currentTimeMillis()}" },
                                    description = description,
                                    categoryId = catId,
                                    basePrice = price.toDoubleOrNull() ?: 499.0,
                                    variants = listOf(
                                        CreateVariantRequest(
                                            title = "Standard",
                                            price = price.toDoubleOrNull() ?: 499.0,
                                            sku = "SKU-${System.currentTimeMillis() % 100000}",
                                            stockQuantity = 100,
                                            isDefault = true
                                        )
                                    ),
                                    images = listOf(
                                        CreateImageRequest(
                                            imageUrl = imageUrl,
                                            isPrimary = true
                                        )
                                    )
                                )
                                repository.createProduct(req)
                                showCreateDialog = false
                                fetchProducts()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Create Product", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- EDIT PRODUCT DIALOG ---
    editingProduct?.let { product ->
        var editTitle by remember(product) { mutableStateOf(product.title) }
        var editPrice by remember(product) { mutableStateOf(product.basePrice.toInt().toString()) }
        var editDesc by remember(product) { mutableStateOf(product.description) }

        AlertDialog(
            onDismissRequest = { editingProduct = null },
            title = { Text("Edit: ${product.title}") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it },
                        label = { Text("Base Price (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val req = UpdateProductRequest(
                                title = editTitle,
                                basePrice = editPrice.toDoubleOrNull(),
                                description = editDesc
                            )
                            repository.updateProduct(product.id, req)
                            editingProduct = null
                            fetchProducts()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Save Changes", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingProduct = null }) { Text("Cancel") }
            }
        )
    }
}
