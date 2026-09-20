package com.wishify.admin.presentation.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wishify.admin.data.model.ChartDataPoint
import com.wishify.admin.data.model.DashboardStats
import com.wishify.admin.data.repository.WishifyAdminRepository
import com.wishify.admin.ui.theme.GoldAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: WishifyAdminRepository,
    onNavigateToOrders: () -> Unit,
    onNavigateToPaymentSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var statsState by remember { mutableStateOf<Result<DashboardStats>?>(null) }
    var chartsState by remember { mutableStateOf<Result<List<ChartDataPoint>>?>(null) }
    val scope = rememberCoroutineScope()

    val fetchStats = {
        scope.launch {
            statsState = null
            chartsState = null
            statsState = repository.getDashboardStats()
            chartsState = repository.getDashboardCharts()
        }
    }

    LaunchedEffect(Unit) {
        fetchStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Operations Dashboard 📊", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { fetchStats() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GoldAccent)
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        val state = statsState
        when {
            state == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldAccent)
                }
            }
            state.isFailure -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Failed to load dashboard data", color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { fetchStats() }) { Text("Retry") }
                }
            }
            state.isSuccess -> {
                val stats = state.getOrNull()!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // KPI Metrics Grid
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                KpiCard(
                                    title = "Today's Revenue",
                                    value = "₹${stats.todayRevenue.toInt()}",
                                    icon = Icons.Default.CurrencyRupee,
                                    color = GoldAccent,
                                    modifier = Modifier.weight(1f)
                                )
                                KpiCard(
                                    title = "Today's Orders",
                                    value = "${stats.todayOrders}",
                                    icon = Icons.Default.ShoppingCart,
                                    color = Color(0xFF64B5F6),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                KpiCard(
                                    title = "Pending Delivery",
                                    value = "${stats.pendingOrders}",
                                    icon = Icons.Default.LocalShipping,
                                    color = Color(0xFFFFB74D),
                                    modifier = Modifier.weight(1f)
                                )
                                KpiCard(
                                    title = "Low Stock Alerts",
                                    value = "${stats.lowStockCount}",
                                    icon = Icons.Default.Warning,
                                    color = if (stats.lowStockCount > 0) Color(0xFFE57373) else Color(0xFF81C784),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Quick Action: UPI QR & Payment Settings
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp),
                            onClick = onNavigateToPaymentSettings,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.QrCode2,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            "UPI QR & Payment Settings",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            "Manage QR Code, UPI ID & COD rules",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }
                    }

                    // 30-Day Sales Chart Representation
                    chartsState?.getOrNull()?.let { chartData ->
                        if (chartData.isNotEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("30-Day Revenue Trend 📈", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(16.dp))

                                        val maxRev = chartData.maxOfOrNull { it.revenue }?.takeIf { it > 0 } ?: 1000.0
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            chartData.takeLast(14).forEach { pt ->
                                                val heightFraction = (pt.revenue / maxRev).coerceIn(0.05, 1.0)
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxHeight(heightFraction.toFloat())
                                                            .width(12.dp)
                                                            .background(GoldAccent, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Past 14 days daily performance",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Top Selling Products
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Top Selling Gifts 🏆", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(12.dp))

                                if (stats.topSellingProducts.isEmpty()) {
                                    Text("No sales data recorded yet", color = Color.Gray, fontSize = 13.sp)
                                } else {
                                    stats.topSellingProducts.forEachIndexed { idx, item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("#${idx + 1}", fontWeight = FontWeight.Bold, color = GoldAccent)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(item.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                                    Text("${item.totalQuantitySold} units sold", fontSize = 12.sp, color = Color.Gray)
                                                }
                                            }
                                            Text("₹${item.totalRevenue.toInt()}", fontWeight = FontWeight.Bold)
                                        }
                                        if (idx < stats.topSellingProducts.size - 1) {
                                            Divider(modifier = Modifier.padding(vertical = 4.dp))
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
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
