package com.wishify.customer.presentation.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wishify.customer.data.local.UserPreferences
import com.wishify.customer.data.model.CreateReminderRequest
import com.wishify.customer.data.model.Product
import com.wishify.customer.data.model.Reminder
import com.wishify.customer.data.repository.WishifyRepository
import com.wishify.customer.presentation.components.EmptyStateView
import com.wishify.customer.presentation.components.ErrorStateView
import com.wishify.customer.presentation.components.LoadingStateView
import com.wishify.customer.presentation.components.ProductCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    repository: WishifyRepository,
    userPreferences: UserPreferences,
    onNavigateToOrders: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val phone by userPreferences.userPhone.collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Account 👤", fontWeight = FontWeight.Bold) })
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
            // Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "W",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Wishify Member",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = phone ?: "+91 98765 43210",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Quick Menu Items
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        AccountMenuItem(
                            icon = Icons.Default.ShoppingBag,
                            title = "My Orders",
                            subtitle = "Track, cancel or reorder gifts",
                            onClick = onNavigateToOrders
                        )
                        Divider()
                        AccountMenuItem(
                            icon = Icons.Default.Favorite,
                            title = "Wishlist",
                            subtitle = "Your saved gifting favorites",
                            onClick = onNavigateToWishlist
                        )
                        Divider()
                        AccountMenuItem(
                            icon = Icons.Default.Notifications,
                            title = "Birthday & Anniversary Reminders",
                            subtitle = "Never forget a special date",
                            onClick = onNavigateToReminders
                        )
                        Divider()
                        AccountMenuItem(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Wishify Wallet",
                            subtitle = "Balance: ₹250.00",
                            onClick = {}
                        )
                    }
                }
            }

            // Logout
            item {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            userPreferences.clearSession()
                            onLogout()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Log Out", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun AccountMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    repository: WishifyRepository,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var wishlistState by remember { mutableStateOf<Result<List<Product>>?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        wishlistState = repository.getWishlist()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wishlist ❤️") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        val state = wishlistState
        when {
            state == null -> LoadingStateView(Modifier.padding(padding))
            state.isFailure -> ErrorStateView(
                message = state.exceptionOrNull()?.message ?: "Failed to load wishlist",
                onRetry = {
                    scope.launch { wishlistState = repository.getWishlist() }
                },
                modifier = Modifier.padding(padding)
            )
            state.isSuccess -> {
                val items = state.getOrNull()!!
                if (items.isEmpty()) {
                    EmptyStateView(
                        title = "Your wishlist is empty",
                        subtitle = "Tap the heart icon on any gift to save it here.",
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(items) { prod ->
                            ProductCard(product = prod, onClick = { onProductClick(prod.slug) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    repository: WishifyRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remindersState by remember { mutableStateOf<Result<List<Reminder>>?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var recipientName by remember { mutableStateOf("") }
    var occasionTitle by remember { mutableStateOf("Birthday") }
    var eventDate by remember { mutableStateOf("2026-10-15") }

    val scope = rememberCoroutineScope()

    val fetchReminders = {
        scope.launch {
            remindersState = null
            remindersState = repository.getReminders()
        }
    }

    LaunchedEffect(Unit) {
        fetchReminders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gift Reminders 🔔") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder", tint = Color.White)
            }
        },
        modifier = modifier
    ) { padding ->
        val state = remindersState
        when {
            state == null -> LoadingStateView(Modifier.padding(padding))
            state.isFailure -> ErrorStateView(
                message = state.exceptionOrNull()?.message ?: "Failed to load reminders",
                onRetry = { fetchReminders() },
                modifier = Modifier.padding(padding)
            )
            state.isSuccess -> {
                val list = state.getOrNull()!!
                if (list.isEmpty()) {
                    EmptyStateView(
                        title = "No reminders added",
                        subtitle = "Add birthdays and anniversaries so you never miss celebrating!",
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
                        items(list) { rem ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Cake,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(rem.recipientName, fontWeight = FontWeight.Bold)
                                        Text(rem.occasionTitle, color = Color.Gray, fontSize = 13.sp)
                                        Text("Date: ${rem.eventDate.take(10)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Occasion Reminder") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = { recipientName = it },
                            label = { Text("Person's Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = occasionTitle,
                            onValueChange = { occasionTitle = it },
                            label = { Text("Occasion (e.g. Birthday)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = eventDate,
                            onValueChange = { eventDate = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                repository.createReminder(
                                    CreateReminderRequest(
                                        occasionTitle = occasionTitle,
                                        recipientName = recipientName,
                                        eventDate = eventDate
                                    )
                                )
                                showAddDialog = false
                                fetchReminders()
                            }
                        },
                        enabled = recipientName.isNotBlank()
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
