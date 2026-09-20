package com.wishify.admin.presentation.customers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wishify.admin.data.model.CustomerSummary
import com.wishify.admin.data.repository.WishifyAdminRepository
import com.wishify.admin.ui.theme.GoldAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    repository: WishifyAdminRepository,
    modifier: Modifier = Modifier
) {
    var customersState by remember { mutableStateOf<Result<List<CustomerSummary>>?>(null) }
    val scope = rememberCoroutineScope()

    val fetchCustomers = {
        scope.launch {
            customersState = null
            customersState = repository.getCustomers()
        }
    }

    LaunchedEffect(Unit) {
        fetchCustomers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registered Customers 👥", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { fetchCustomers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GoldAccent)
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        val state = customersState
        when {
            state == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldAccent)
                }
            }
            state.isFailure -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Failed to load customers: ${state.exceptionOrNull()?.message}", color = Color.Red)
                }
            }
            state.isSuccess -> {
                val customers = state.getOrNull()!!
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    items(customers) { customer ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(customer.name ?: "Anonymous Customer", fontWeight = FontWeight.Bold)
                                Text(customer.phone ?: customer.email ?: "No contact info", fontSize = 13.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Orders: ${customer.orderCount}", fontSize = 12.sp)
                                    Text("Lifetime Spend: ₹${customer.totalSpend.toInt()}", fontWeight = FontWeight.Bold, color = GoldAccent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
