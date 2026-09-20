package com.wishify.customer.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Categories : Screen("categories")
    object Occasions : Screen("occasions")
    object Cart : Screen("cart")
    object Account : Screen("account")
    
    object ProductList : Screen("products?categoryId={categoryId}&occasionId={occasionId}&title={title}") {
        fun createRoute(categoryId: String? = null, occasionId: String? = null, title: String? = null): String {
            val params = mutableListOf<String>()
            categoryId?.let { params.add("categoryId=$it") }
            occasionId?.let { params.add("occasionId=$it") }
            title?.let { params.add("title=$it") }
            return if (params.isEmpty()) "products" else "products?${params.joinToString("&")}"
        }
    }
    
    object ProductDetail : Screen("product/{slug}") {
        fun createRoute(slug: String): String = "product/$slug"
    }

    object Checkout : Screen("checkout")
    
    object OrderDetail : Screen("order/{id}") {
        fun createRoute(id: String): String = "order/$id"
    }

    object Orders : Screen("orders")
    object Wishlist : Screen("wishlist")
    object Reminders : Screen("reminders")
    object Login : Screen("login")
    object Otp : Screen("otp?phone={phone}") {
        fun createRoute(phone: String): String = "otp?phone=$phone"
    }
}
