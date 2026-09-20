package com.wishify.admin.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
    val meta: ApiMeta? = null
)

@Serializable
data class ApiError(
    val code: String,
    val message: String
)

@Serializable
data class ApiMeta(
    val page: Int? = null,
    val limit: Int? = null,
    val total: Int? = null,
    val totalPages: Int? = null
)

@Serializable
data class AdminLoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AdminAuthResponse(
    val user: AdminUser,
    val tokens: TokenModel
)

@Serializable
data class AdminUser(
    val id: String,
    val name: String? = null,
    val email: String,
    val role: String
)

@Serializable
data class TokenModel(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int? = null
)

@Serializable
data class DashboardStats(
    val totalOrders: Int,
    val todayOrders: Int,
    val totalRevenue: Double,
    val todayRevenue: Double,
    val pendingOrders: Int,
    val lowStockCount: Int,
    val topSellingProducts: List<TopProduct> = emptyList()
)

@Serializable
data class TopProduct(
    val productId: String,
    val title: String,
    val totalQuantitySold: Int,
    val totalRevenue: Double
)

@Serializable
data class ChartDataPoint(
    val date: String,
    val revenue: Double,
    val orderCount: Int
)

@Serializable
data class AdminOrder(
    val id: String,
    val orderNumber: String,
    val subtotal: Double,
    val deliveryFee: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val orderStatus: String,
    val paymentStatus: String,
    val paymentMethod: String,
    val deliveryDate: String,
    val recipientName: String,
    val recipientPhone: String,
    val giftMessage: String? = null,
    val isSenderHidden: Boolean = false,
    val deliveryPartnerName: String? = null,
    val deliveryPartnerPhone: String? = null,
    val trackingNumber: String? = null,
    val items: List<AdminOrderItem> = emptyList(),
    val statusHistory: List<AdminStatusHistory> = emptyList(),
    val user: CustomerSummary? = null,
    val createdAt: String
)

@Serializable
data class AdminOrderItem(
    val id: String,
    val title: String,
    val variantTitle: String? = null,
    val unitPrice: Double,
    val quantity: Int,
    val totalPrice: Double,
    val personalizationText: String? = null,
    val imageUrl: String? = null
)

@Serializable
data class AdminStatusHistory(
    val id: String,
    val status: String,
    val notes: String? = null,
    val createdAt: String
)

@Serializable
data class UpdateStatusRequest(
    val status: String,
    val notes: String? = null
)

@Serializable
data class AssignDeliveryRequest(
    val deliveryPartnerName: String,
    val deliveryPartnerPhone: String,
    val trackingNumber: String? = null
)

@Serializable
data class AdminProduct(
    val id: String,
    val title: String,
    val slug: String,
    val description: String,
    val basePrice: Double,
    val compareAtPrice: Double? = null,
    val isVegetarian: Boolean = true,
    val isPersonalized: Boolean = false,
    val personalizationPrompt: String? = null,
    val isSameDayEligible: Boolean = true,
    val isMidnightEligible: Boolean = true,
    val isActive: Boolean = true,
    val averageRating: Double = 5.0,
    val reviewCount: Int = 0,
    val variants: List<AdminVariant> = emptyList(),
    val images: List<AdminImage> = emptyList()
)

@Serializable
data class AdminVariant(
    val id: String,
    val title: String,
    val price: Double,
    val compareAtPrice: Double? = null,
    val sku: String,
    val stockQuantity: Int = 100,
    val weightOrSize: String? = null,
    val isDefault: Boolean = false
)

@Serializable
data class AdminImage(
    val id: String,
    val imageUrl: String,
    val isPrimary: Boolean = false,
    val sortOrder: Int = 0
)

@Serializable
data class CustomerSummary(
    val id: String,
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val walletBalance: Double = 0.0,
    val orderCount: Int = 0,
    val totalSpend: Double = 0.0,
    val createdAt: String? = null
)

@Serializable
data class AdminReview(
    val id: String,
    val rating: Int,
    val comment: String? = null,
    val status: String,
    val createdAt: String,
    val product: ProductRef? = null,
    val user: UserRef? = null
)

@Serializable
data class ProductRef(
    val id: String,
    val title: String,
    val slug: String
)

@Serializable
data class UserRef(
    val id: String,
    val name: String? = null,
    val phone: String? = null
)

@Serializable
data class UpdateReviewRequest(
    val status: String
)

@Serializable
data class BroadcastPushRequest(
    val title: String,
    val body: String,
    val type: String = "PROMOTIONAL"
)

// --- PRODUCT CRUD REQUESTS ---
@Serializable
data class CreateProductRequest(
    val title: String,
    val slug: String,
    val description: String,
    val categoryId: String,
    val occasionId: String? = null,
    val basePrice: Double,
    val compareAtPrice: Double? = null,
    val isVegetarian: Boolean = true,
    val isPersonalized: Boolean = false,
    val personalizationPrompt: String? = null,
    val isSameDayEligible: Boolean = true,
    val isMidnightEligible: Boolean = true,
    val variants: List<CreateVariantRequest>,
    val images: List<CreateImageRequest>,
    val addOnIds: List<String> = emptyList()
)

@Serializable
data class CreateVariantRequest(
    val title: String,
    val price: Double,
    val compareAtPrice: Double? = null,
    val sku: String,
    val stockQuantity: Int = 100,
    val weightOrSize: String? = null,
    val isDefault: Boolean = false
)

@Serializable
data class CreateImageRequest(
    val imageUrl: String,
    val isPrimary: Boolean = false,
    val sortOrder: Int = 0
)

@Serializable
data class UpdateProductRequest(
    val title: String? = null,
    val description: String? = null,
    val basePrice: Double? = null,
    val compareAtPrice: Double? = null,
    val isVegetarian: Boolean? = null,
    val isPersonalized: Boolean? = null,
    val personalizationPrompt: String? = null,
    val isSameDayEligible: Boolean? = null,
    val isMidnightEligible: Boolean? = null,
    val isActive: Boolean? = null
)

// --- CATALOG ENTITIES & REQUESTS ---
@Serializable
data class AdminCategory(
    val id: String,
    val name: String,
    val slug: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val slug: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val sortOrder: Int = 0
)

@Serializable
data class AdminOccasion(
    val id: String,
    val name: String,
    val slug: String,
    val description: String? = null,
    val bannerUrl: String? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

@Serializable
data class CreateOccasionRequest(
    val name: String,
    val slug: String,
    val description: String? = null,
    val bannerUrl: String? = null,
    val sortOrder: Int = 0
)

@Serializable
data class AdminBanner(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String,
    val deepLink: String? = null,
    val position: String = "HOME_TOP",
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

@Serializable
data class CreateBannerRequest(
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String,
    val deepLink: String? = null,
    val position: String = "HOME_TOP",
    val sortOrder: Int = 0
)

@Serializable
data class AdminCoupon(
    val id: String,
    val code: String,
    val description: String? = null,
    val discountType: String = "PERCENTAGE",
    val discountValue: Double,
    val minOrderValue: Double = 0.0,
    val maxDiscountAmount: Double? = null,
    val validFrom: String? = null,
    val validTo: String,
    val usageLimit: Int = 1000,
    val usedCount: Int = 0,
    val isActive: Boolean = true
)

@Serializable
data class CreateCouponRequest(
    val code: String,
    val description: String? = null,
    val discountType: String = "PERCENTAGE",
    val discountValue: Double,
    val minOrderValue: Double = 0.0,
    val maxDiscountAmount: Double? = null,
    val validFrom: String? = null,
    val validTo: String,
    val usageLimit: Int = 1000
)

@Serializable
data class AdminPincode(
    val id: String,
    val code: String,
    val city: String,
    val state: String,
    val isServiceable: Boolean = true,
    val isSameDayAvailable: Boolean = true,
    val isMidnightAvailable: Boolean = true,
    val standardDeliveryFee: Double = 0.0,
    val midnightDeliveryFee: Double = 250.0
)

@Serializable
data class CreatePincodeRequest(
    val code: String,
    val city: String,
    val state: String,
    val isServiceable: Boolean = true,
    val isSameDayAvailable: Boolean = true,
    val isMidnightAvailable: Boolean = true,
    val standardDeliveryFee: Double = 0.0,
    val midnightDeliveryFee: Double = 250.0
)

@Serializable
data class AdminSlot(
    val id: String,
    val title: String,
    val slotType: String = "STANDARD",
    val startTime: String,
    val endTime: String,
    val fee: Double = 0.0,
    val cutoffHoursBefore: Int = 2,
    val isActive: Boolean = true
)

@Serializable
data class CreateSlotRequest(
    val title: String,
    val slotType: String = "STANDARD",
    val startTime: String,
    val endTime: String,
    val fee: Double = 0.0,
    val cutoffHoursBefore: Int = 2
)
