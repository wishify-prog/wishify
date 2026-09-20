package com.wishify.admin.data.remote

import com.wishify.admin.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface WishifyAdminApi {

    @POST("api/v1/admin/auth/login")
    suspend fun login(@Body req: AdminLoginRequest): Response<ApiResponse<AdminAuthResponse>>

    @GET("api/v1/admin/dashboard/stats")
    suspend fun getDashboardStats(): Response<ApiResponse<DashboardStats>>

    @GET("api/v1/admin/dashboard/charts")
    suspend fun getDashboardCharts(): Response<ApiResponse<List<ChartDataPoint>>>

    @GET("api/v1/admin/orders")
    suspend fun getOrders(
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 30
    ): Response<ApiResponse<List<AdminOrder>>>

    @GET("api/v1/admin/orders/{id}")
    suspend fun getOrderDetail(@Path("id") id: String): Response<ApiResponse<AdminOrder>>

    @PATCH("api/v1/admin/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body req: UpdateStatusRequest
    ): Response<ApiResponse<AdminOrder>>

    @POST("api/v1/admin/orders/{id}/assign")
    suspend fun assignDelivery(
        @Path("id") id: String,
        @Body req: AssignDeliveryRequest
    ): Response<ApiResponse<AdminOrder>>

    @POST("api/v1/admin/orders/{id}/refund")
    suspend fun refundOrder(@Path("id") id: String): Response<ApiResponse<Map<String, String>>>

    @GET("api/v1/admin/products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 50
    ): Response<ApiResponse<List<AdminProduct>>>

    @DELETE("api/v1/admin/products/{id}")
    suspend fun toggleProductActive(@Path("id") id: String): Response<ApiResponse<Map<String, String>>>

    @GET("api/v1/admin/customers")
    suspend fun getCustomers(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 50
    ): Response<ApiResponse<List<CustomerSummary>>>

    @GET("api/v1/admin/reviews")
    suspend fun getReviews(
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<AdminReview>>>

    @PATCH("api/v1/admin/reviews/{id}")
    suspend fun updateReview(
        @Path("id") id: String,
        @Body req: UpdateReviewRequest
    ): Response<ApiResponse<AdminReview>>

    @POST("api/v1/admin/broadcast/push")
    suspend fun broadcastPush(
        @Body req: BroadcastPushRequest
    ): Response<ApiResponse<Map<String, String>>>

    // --- PRODUCTS CRUD ---
    @POST("api/v1/admin/products")
    suspend fun createProduct(@Body req: CreateProductRequest): Response<ApiResponse<AdminProduct>>

    @PUT("api/v1/admin/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body req: UpdateProductRequest
    ): Response<ApiResponse<AdminProduct>>

    @DELETE("api/v1/admin/products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: String,
        @Query("permanent") permanent: Boolean? = null
    ): Response<ApiResponse<Map<String, String>>>

    // --- CATEGORIES CRUD ---
    @GET("api/v1/admin/categories")
    suspend fun getCategories(): Response<ApiResponse<List<AdminCategory>>>

    @POST("api/v1/admin/categories")
    suspend fun createCategory(@Body req: CreateCategoryRequest): Response<ApiResponse<AdminCategory>>

    @PUT("api/v1/admin/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body req: CreateCategoryRequest
    ): Response<ApiResponse<AdminCategory>>

    @DELETE("api/v1/admin/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<ApiResponse<Map<String, String>>>

    // --- OCCASIONS CRUD ---
    @GET("api/v1/admin/occasions")
    suspend fun getOccasions(): Response<ApiResponse<List<AdminOccasion>>>

    @POST("api/v1/admin/occasions")
    suspend fun createOccasion(@Body req: CreateOccasionRequest): Response<ApiResponse<AdminOccasion>>

    @PUT("api/v1/admin/occasions/{id}")
    suspend fun updateOccasion(
        @Path("id") id: String,
        @Body req: CreateOccasionRequest
    ): Response<ApiResponse<AdminOccasion>>

    @DELETE("api/v1/admin/occasions/{id}")
    suspend fun deleteOccasion(@Path("id") id: String): Response<ApiResponse<Map<String, String>>>

    // --- BANNERS CRUD ---
    @GET("api/v1/admin/banners")
    suspend fun getBanners(): Response<ApiResponse<List<AdminBanner>>>

    @POST("api/v1/admin/banners")
    suspend fun createBanner(@Body req: CreateBannerRequest): Response<ApiResponse<AdminBanner>>

    @PUT("api/v1/admin/banners/{id}")
    suspend fun updateBanner(
        @Path("id") id: String,
        @Body req: CreateBannerRequest
    ): Response<ApiResponse<AdminBanner>>

    @DELETE("api/v1/admin/banners/{id}")
    suspend fun deleteBanner(@Path("id") id: String): Response<ApiResponse<Map<String, String>>>

    // --- COUPONS CRUD ---
    @GET("api/v1/admin/coupons")
    suspend fun getCoupons(): Response<ApiResponse<List<AdminCoupon>>>

    @POST("api/v1/admin/coupons")
    suspend fun createCoupon(@Body req: CreateCouponRequest): Response<ApiResponse<AdminCoupon>>

    @PUT("api/v1/admin/coupons/{id}")
    suspend fun updateCoupon(
        @Path("id") id: String,
        @Body req: CreateCouponRequest
    ): Response<ApiResponse<AdminCoupon>>

    @DELETE("api/v1/admin/coupons/{id}")
    suspend fun deleteCoupon(@Path("id") id: String): Response<ApiResponse<Map<String, String>>>

    // --- PINCODES CRUD ---
    @GET("api/v1/admin/pincodes")
    suspend fun getPincodes(): Response<ApiResponse<List<AdminPincode>>>

    @POST("api/v1/admin/pincodes")
    suspend fun createPincode(@Body req: CreatePincodeRequest): Response<ApiResponse<AdminPincode>>

    @PUT("api/v1/admin/pincodes/{id}")
    suspend fun updatePincode(
        @Path("id") id: String,
        @Body req: CreatePincodeRequest
    ): Response<ApiResponse<AdminPincode>>

    @DELETE("api/v1/admin/pincodes/{id}")
    suspend fun deletePincode(@Path("id") id: String): Response<ApiResponse<Map<String, String>>>

    // --- DELIVERY SLOTS CRUD ---
    @GET("api/v1/admin/slots")
    suspend fun getSlots(): Response<ApiResponse<List<AdminSlot>>>

    @POST("api/v1/admin/slots")
    suspend fun createSlot(@Body req: CreateSlotRequest): Response<ApiResponse<AdminSlot>>

    @PUT("api/v1/admin/slots/{id}")
    suspend fun updateSlot(
        @Path("id") id: String,
        @Body req: CreateSlotRequest
    ): Response<ApiResponse<AdminSlot>>

    @DELETE("api/v1/admin/slots/{id}")
    suspend fun deleteSlot(@Path("id") id: String): Response<ApiResponse<Map<String, String>>>
}
