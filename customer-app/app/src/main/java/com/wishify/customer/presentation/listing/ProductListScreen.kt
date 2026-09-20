package com.wishify.customer.presentation.listing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wishify.customer.data.model.Product
import com.wishify.customer.data.repository.WishifyRepository
import com.wishify.customer.presentation.components.EmptyStateView
import com.wishify.customer.presentation.components.ErrorStateView
import com.wishify.customer.presentation.components.LoadingStateView
import com.wishify.customer.presentation.components.ProductCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    title: String,
    categoryId: String?,
    occasionId: String?,
    repository: WishifyRepository,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var productsState by remember { mutableStateOf<Result<List<Product>>?>(null) }
    var selectedSort by remember { mutableStateOf<String?>(null) }
    var filterSameDayOnly by remember { mutableStateOf(false) }
    var filterUnder999Only by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val fetchProducts = {
        scope.launch {
            productsState = null
            val maxPrice = if (filterUnder999Only) 999.0 else null
            productsState = repository.getProducts(
                categoryId = if (categoryId.isNullOrBlank()) null else categoryId,
                occasionId = if (occasionId.isNullOrBlank()) null else occasionId,
                maxPrice = maxPrice,
                sort = selectedSort
            )
        }
    }

    LaunchedEffect(selectedSort, filterUnder999Only) {
        fetchProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title.ifBlank { "All Gifts" }) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Filter / Sort chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedSort == "price_asc",
                    onClick = {
                        selectedSort = if (selectedSort == "price_asc") null else "price_asc"
                    },
                    label = { Text("Price: Low to High") }
                )
                FilterChip(
                    selected = filterUnder999Only,
                    onClick = { filterUnder999Only = !filterUnder999Only },
                    label = { Text("Under ₹999") }
                )
                FilterChip(
                    selected = filterSameDayOnly,
                    onClick = { filterSameDayOnly = !filterSameDayOnly },
                    label = { Text("Same-Day") }
                )
            }

            val state = productsState
            when {
                state == null -> LoadingStateView()
                state.isFailure -> ErrorStateView(
                    message = state.exceptionOrNull()?.message ?: "Failed to load products",
                    onRetry = { fetchProducts() }
                )
                state.isSuccess -> {
                    var items = state.getOrNull()!!
                    if (filterSameDayOnly) {
                        items = items.filter { it.isSameDayEligible }
                    }

                    if (items.isEmpty()) {
                        EmptyStateView(
                            title = "No gifts found",
                            subtitle = "Try adjusting your filters or browsing other categories."
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(items) { product ->
                                ProductCard(
                                    product = product,
                                    onClick = { onProductClick(product.slug) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
