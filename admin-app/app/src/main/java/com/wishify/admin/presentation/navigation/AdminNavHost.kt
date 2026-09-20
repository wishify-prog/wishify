package com.wishify.admin.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.wishify.admin.data.local.AdminPreferences
import com.wishify.admin.data.repository.WishifyAdminRepository
import com.wishify.admin.presentation.auth.AdminLoginScreen
import com.wishify.admin.presentation.broadcast.BroadcastPushScreen
import com.wishify.admin.presentation.catalog.AdminCatalogScreen
import com.wishify.admin.presentation.customers.CustomerListScreen
import com.wishify.admin.presentation.dashboard.DashboardScreen
import com.wishify.admin.presentation.orders.OrderPipelineScreen
import com.wishify.admin.presentation.products.AdminProductsScreen
import com.wishify.admin.presentation.reviews.ReviewModerationScreen
import com.wishify.admin.ui.theme.GoldAccent

sealed class AdminScreen(val route: String) {
    object Login : AdminScreen("login")
    object Dashboard : AdminScreen("dashboard")
    object Orders : AdminScreen("orders")
    object Products : AdminScreen("products")
    object Catalog : AdminScreen("catalog")
    object Customers : AdminScreen("customers")
    object Reviews : AdminScreen("reviews")
    object Broadcast : AdminScreen("broadcast")
}

data class AdminNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun AdminNavHost(
    repository: WishifyAdminRepository,
    adminPreferences: AdminPreferences,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        AdminNavItem(AdminScreen.Dashboard.route, "Dashboard", Icons.Default.Dashboard),
        AdminNavItem(AdminScreen.Orders.route, "Orders", Icons.Default.LocalShipping),
        AdminNavItem(AdminScreen.Products.route, "Products", Icons.Default.Inventory2),
        AdminNavItem(AdminScreen.Catalog.route, "Catalog", Icons.Default.Category),
        AdminNavItem(AdminScreen.Customers.route, "Customers", Icons.Default.People),
        AdminNavItem(AdminScreen.Reviews.route, "Reviews", Icons.Default.RateReview),
        AdminNavItem(AdminScreen.Broadcast.route, "Broadcast", Icons.Default.Campaign)
    )

    val showBottomBar = navItems.any { it.route == currentRoute }

    val token by adminPreferences.accessToken.collectAsState(initial = null)

    LaunchedEffect(token) {
        if (!token.isNullOrBlank() && currentRoute == AdminScreen.Login.route) {
            navController.navigate(AdminScreen.Dashboard.route) {
                popUpTo(AdminScreen.Login.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GoldAccent,
                                selectedTextColor = GoldAccent,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AdminScreen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AdminScreen.Login.route) {
                AdminLoginScreen(
                    repository = repository,
                    adminPreferences = adminPreferences,
                    onLoginSuccess = {
                        navController.navigate(AdminScreen.Dashboard.route) {
                            popUpTo(AdminScreen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(AdminScreen.Dashboard.route) {
                DashboardScreen(
                    repository = repository,
                    onNavigateToOrders = { navController.navigate(AdminScreen.Orders.route) }
                )
            }

            composable(AdminScreen.Orders.route) {
                OrderPipelineScreen(repository = repository)
            }

            composable(AdminScreen.Products.route) {
                AdminProductsScreen(repository = repository)
            }

            composable(AdminScreen.Catalog.route) {
                AdminCatalogScreen(repository = repository)
            }

            composable(AdminScreen.Customers.route) {
                CustomerListScreen(repository = repository)
            }

            composable(AdminScreen.Reviews.route) {
                ReviewModerationScreen(repository = repository)
            }

            composable(AdminScreen.Broadcast.route) {
                BroadcastPushScreen(repository = repository)
            }
        }
    }
}
