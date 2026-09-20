package com.wishify.customer.data.remote

import com.wishify.customer.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface WishifyCustomerApi {

    // Auth
    @POST("api/v1/auth/request-otp")
    suspend fun requestOtp(@Body req: RequestOtpRequest): Response<ApiResponse<Map<String, String>>>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body req: VerifyOtpRequest): Response<ApiResponse<AuthResponse>>

    @GET("api/v1/auth/me")
    suspend fun getProfile(): Response<ApiResponse<UserModel>>

    // Home & Catalog
    @GET("api/v1/home")
    suspend fun getHomeFeed(): Response<ApiResponse<HomeFeed>>

    @GET("api/v1/categories")
    suspend fun getCategories(): Response<ApiResponse<List<Category>>>

    @GET("api/v1/occasions")
    suspend fun getOccasions(): Response<ApiResponse<List<Occasion>>>

    @GET("api/v1/products")
    suspend fun getProducts(
        @Query("categoryId") categoryId: String? = null,
        @Query("occasionId") occasionId: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("isVegetarian") isVegetarian: Boolean? = null,
        @Query("isSameDay") isSameDay: Boolean? = null,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 20
    ): Response<ApiResponse<List<Product>>>

    @GET("api/v1/products/search")
    suspend fun searchProducts(@Query("q") query: String): Response<ApiResponse<Map<String, List<Product>>>>

    @GET("api/v1/products/{slug}")
    suspend fun getProductDetail(@Path("slug") slug: String): Response<ApiResponse<ProductDetailResponse>>

    // Delivery & Pincode
    @GET("api/v1/pincode/check/{code}")
    suspend fun checkPincode(@Path("code") code: String): Response<ApiResponse<PincodeCheckResponse>>

    @GET("api/v1/delivery-slots")
    suspend fun getDeliverySlots(
        @Query("pincode") pincode: String? = null,
        @Query("date") date: String? = null
    ): Response<ApiResponse<List<DeliverySlot>>>

    // Cart
    @GET("api/v1/cart")
    suspend fun getCart(): Response<ApiResponse<CartResponse>>

    @POST("api/v1/cart/items")
    suspend fun addToCart(@Body req: AddToCartRequest): Response<ApiResponse<CartResponse>>

    @DELETE("api/v1/cart/items/{id}")
    suspend fun removeCartItem(@Path("id") id: String): Response<ApiResponse<CartResponse>>

    // Coupons
    @GET("api/v1/coupons")
    suspend fun getCoupons(): Response<ApiResponse<List<Coupon>>>

    @POST("api/v1/coupons/validate")
    suspend fun validateCoupon(@Body req: ValidateCouponRequest): Response<ApiResponse<ValidateCouponResponse>>

    // Addresses
    @GET("api/v1/addresses")
    suspend fun getAddresses(): Response<ApiResponse<List<Address>>>

    @POST("api/v1/addresses")
    suspend fun addAddress(@Body address: Address): Response<ApiResponse<Address>>

    // Checkout & Orders
    @GET("api/v1/payment/settings")
    suspend fun getPaymentSettings(): Response<ApiResponse<PaymentSetting>>

    @POST("api/v1/checkout/initiate")
    suspend fun initiateCheckout(@Body req: InitiateCheckoutRequest): Response<ApiResponse<InitiateCheckoutResponse>>

    @POST("api/v1/checkout/verify")
    suspend fun verifyPayment(@Body req: VerifyPaymentRequest): Response<ApiResponse<Map<String, String>>>

    @GET("api/v1/orders")
    suspend fun getOrders(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 20
    ): Response<ApiResponse<List<Order>>>

    @GET("api/v1/orders/{id}")
    suspend fun getOrderDetail(@Path("id") orderId: String): Response<ApiResponse<Order>>

    @POST("api/v1/orders/{id}/cancel")
    suspend fun cancelOrder(
        @Path("id") orderId: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Order>>

    // Wishlist
    @GET("api/v1/wishlist")
    suspend fun getWishlist(): Response<ApiResponse<List<Product>>>

    @POST("api/v1/wishlist/{productId}")
    suspend fun addToWishlist(@Path("productId") productId: String): Response<ApiResponse<Map<String, String>>>

    @DELETE("api/v1/wishlist/{productId}")
    suspend fun removeFromWishlist(@Path("productId") productId: String): Response<ApiResponse<Map<String, String>>>

    // Reminders
    @GET("api/v1/reminders")
    suspend fun getReminders(): Response<ApiResponse<List<Reminder>>>

    @POST("api/v1/reminders")
    suspend fun createReminder(@Body req: CreateReminderRequest): Response<ApiResponse<Reminder>>

    @DELETE("api/v1/reminders/{id}")
    suspend fun deleteReminder(@Path("id") id: String): Response<ApiResponse<Map<String, String>>>

    // Notifications
    @GET("api/v1/notifications")
    suspend fun getNotifications(): Response<ApiResponse<List<NotificationModel>>>
}
