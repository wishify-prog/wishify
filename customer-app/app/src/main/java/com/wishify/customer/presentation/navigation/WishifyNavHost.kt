package com.wishify.customer.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.wishify.customer.data.local.UserPreferences
import com.wishify.customer.data.repository.WishifyRepository
import com.wishify.customer.presentation.account.AccountScreen
import com.wishify.customer.presentation.account.RemindersScreen
import com.wishify.customer.presentation.account.WishlistScreen
import com.wishify.customer.presentation.auth.LoginScreen
import com.wishify.customer.presentation.auth.OtpScreen
import com.wishify.customer.presentation.cart.CartScreen
import com.wishify.customer.presentation.checkout.CheckoutScreen
import com.wishify.customer.presentation.home.HomeScreen
import com.wishify.customer.presentation.listing.ProductListScreen
import com.wishify.customer.presentation.orders.OrderDetailScreen
import com.wishify.customer.presentation.orders.OrderHistoryScreen
import com.wishify.customer.presentation.product.ProductDetailScreen

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun WishifyNavHost(
    repository: WishifyRepository,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedPincode by userPreferences.selectedPincode.collectAsState(initial = "560001")

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home),
        BottomNavItem(Screen.Categories.route, "Categories", Icons.Default.Category),
        BottomNavItem(Screen.Occasions.route, "Occasions", Icons.Default.Celebration),
        BottomNavItem(Screen.Cart.route, "Cart", Icons.Default.ShoppingCart),
        BottomNavItem(Screen.Account.route, "Account", Icons.Default.Person)
    )

    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    bottomNavItems.forEach { item ->
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
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Home
            composable(Screen.Home.route) {
                HomeScreen(
                    repository = repository,
                    selectedPincode = selectedPincode,
                    onPincodeClick = { /* Pincode dialog */ },
                    onCategoryClick = { id, title ->
                        navController.navigate(Screen.ProductList.createRoute(categoryId = id, title = title))
                    },
                    onOccasionClick = { id, title ->
                        navController.navigate(Screen.ProductList.createRoute(occasionId = id, title = title))
                    },
                    onProductClick = { slug ->
                        navController.navigate(Screen.ProductDetail.createRoute(slug))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.ProductList.createRoute(title = "Search Gifts"))
                    }
                )
            }

            // 2. Categories
            composable(Screen.Categories.route) {
                ProductListScreen(
                    title = "All Categories",
                    categoryId = null,
                    occasionId = null,
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { slug -> navController.navigate(Screen.ProductDetail.createRoute(slug)) }
                )
            }

            // 3. Occasions
            composable(Screen.Occasions.route) {
                ProductListScreen(
                    title = "Occasions",
                    categoryId = null,
                    occasionId = null,
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { slug -> navController.navigate(Screen.ProductDetail.createRoute(slug)) }
                )
            }

            // 4. Cart
            composable(Screen.Cart.route) {
                CartScreen(
                    repository = repository,
                    onProceedToCheckout = { navController.navigate(Screen.Checkout.route) },
                    onContinueShopping = { navController.navigate(Screen.Home.route) }
                )
            }

            // 5. Account
            composable(Screen.Account.route) {
                AccountScreen(
                    repository = repository,
                    userPreferences = userPreferences,
                    onNavigateToOrders = { navController.navigate(Screen.Orders.route) },
                    onNavigateToWishlist = { navController.navigate(Screen.Wishlist.route) },
                    onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // 6. Product Listing
            composable(
                route = Screen.ProductList.route,
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.StringType; nullable = true },
                    navArgument("occasionId") { type = NavType.StringType; nullable = true },
                    navArgument("title") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId")
                val occasionId = backStackEntry.arguments?.getString("occasionId")
                val title = backStackEntry.arguments?.getString("title") ?: "Products"
                ProductListScreen(
                    title = title,
                    categoryId = categoryId,
                    occasionId = occasionId,
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { slug -> navController.navigate(Screen.ProductDetail.createRoute(slug)) }
                )
            }

            // 7. Product Detail
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug") ?: ""
                ProductDetailScreen(
                    slug = slug,
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onAddToCartSuccess = { navController.navigate(Screen.Cart.route) },
                    onBuyNowClick = { navController.navigate(Screen.Checkout.route) },
                    onProductClick = { newSlug -> navController.navigate(Screen.ProductDetail.createRoute(newSlug)) }
                )
            }

            // 8. Checkout
            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    repository = repository,
                    userPreferences = userPreferences,
                    onBackClick = { navController.popBackStack() },
                    onOrderPlaced = { orderId ->
                        navController.navigate(Screen.OrderDetail.createRoute(orderId)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            // 9. Order Detail
            composable(
                route = Screen.OrderDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("id") ?: ""
                OrderDetailScreen(
                    orderId = orderId,
                    repository = repository,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 10. Orders
            composable(Screen.Orders.route) {
                OrderHistoryScreen(
                    repository = repository,
                    onOrderClick = { orderId ->
                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                    }
                )
            }

            // 11. Wishlist
            composable(Screen.Wishlist.route) {
                WishlistScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { slug -> navController.navigate(Screen.ProductDetail.createRoute(slug)) }
                )
            }

            // 12. Reminders
            composable(Screen.Reminders.route) {
                RemindersScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 13. Login
            composable(Screen.Login.route) {
                LoginScreen(
                    repository = repository,
                    onOtpSent = { phone -> navController.navigate(Screen.Otp.createRoute(phone)) },
                    onContinueAsGuest = { navController.navigate(Screen.Home.route) }
                )
            }

            // 14. OTP
            composable(
                route = Screen.Otp.route,
                arguments = listOf(navArgument("phone") { type = NavType.StringType })
            ) { backStackEntry ->
                val phone = backStackEntry.arguments?.getString("phone") ?: ""
                OtpScreen(
                    phone = phone,
                    repository = repository,
                    userPreferences = userPreferences,
                    onAuthSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
