package com.wishify.customer.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import com.wishify.customer.data.model.HomeFeed
import com.wishify.customer.data.remote.RealtimeSyncManager
import com.wishify.customer.data.repository.WishifyRepository
import com.wishify.customer.presentation.components.ErrorStateView
import com.wishify.customer.presentation.components.LoadingStateView
import com.wishify.customer.presentation.components.ProductCard
import com.wishify.customer.presentation.components.SectionHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: WishifyRepository,
    selectedPincode: String,
    onPincodeClick: () -> Unit,
    onCategoryClick: (String, String) -> Unit,
    onOccasionClick: (String, String) -> Unit,
    onProductClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var homeFeedState by remember { mutableStateOf<Result<HomeFeed>?>(null) }
    val scope = rememberCoroutineScope()

    val loadData = {
        scope.launch {
            homeFeedState = repository.getHomeFeed()
        }
    }

    LaunchedEffect(Unit) {
        loadData()
        RealtimeSyncManager.events.collect { eventJson ->
            if (eventJson.contains("CATALOG_UPDATED") ||
                eventJson.contains("BANNER_UPDATED") ||
                eventJson.contains("PRODUCT_UPDATED")
            ) {
                loadData()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable { onPincodeClick() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Pincode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Deliver to: $selectedPincode",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { padding ->
        val state = homeFeedState
        when {
            state == null -> LoadingStateView(Modifier.padding(padding))
            state.isFailure -> ErrorStateView(
                message = state.exceptionOrNull()?.message ?: "Failed to load feed",
                onRetry = { loadData() },
                modifier = Modifier.padding(padding)
            )
            state.isSuccess -> {
                val feed = state.getOrNull()!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Search Bar Mock / Nav Trigger
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSearchClick() },
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Search flowers, cakes, plants, gifts...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Banners Carousel
                    if (feed.banners.isNotEmpty()) {
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                items(feed.banners) { banner ->
                                    Card(
                                        modifier = Modifier
                                            .width(320.dp)
                                            .height(140.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Box {
                                            AsyncImage(
                                                model = banner.imageUrl,
                                                contentDescription = banner.title,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.35f))
                                                    .padding(16.dp),
                                                contentAlignment = Alignment.BottomStart
                                            ) {
                                                Column {
                                                    Text(
                                                        text = banner.title,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                    banner.subtitle?.let {
                                                        Text(
                                                            text = it,
                                                            color = Color.White.copy(alpha = 0.9f),
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
                    }

                    // Category Circular Row
                    if (feed.categories.isNotEmpty()) {
                        item {
                            Text(
                                text = "What are you looking for?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(feed.categories) { cat ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(72.dp)
                                            .clickable { onCategoryClick(cat.id, cat.name) }
                                    ) {
                                        AsyncImage(
                                            model = cat.imageUrl ?: "",
                                            contentDescription = cat.name,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Occasions Row
                    if (feed.occasions.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Celebrate Occasions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(feed.occasions) { occ ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { onOccasionClick(occ.id, occ.name) },
                                        label = { Text(occ.name) }
                                    )
                                }
                            }
                        }
                    }

                    // Curated Sections (Trending, Best Sellers, Same-Day, Under ₹999)
                    feed.sections.forEach { section ->
                        if (section.products.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                SectionHeader(
                                    title = section.title,
                                    subtitle = section.subtitle,
                                    onViewAllClick = {
                                        // Navigate to full list
                                        onCategoryClick("", section.title)
                                    }
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(section.products) { prod ->
                                        ProductCard(
                                            product = prod,
                                            onClick = { onProductClick(prod.slug) }
                                        )
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
