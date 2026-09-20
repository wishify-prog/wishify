package com.wishify.customer.data.model

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
data class AuthResponse(
    val user: UserModel,
    val tokens: TokenModel
)

@Serializable
data class UserModel(
    val id: String,
    val phone: String? = null,
    val email: String? = null,
    val name: String? = null,
    val walletBalance: Double = 0.0
)

@Serializable
data class TokenModel(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int? = null
)

@Serializable
data class RequestOtpRequest(
    val phone: String
)

@Serializable
data class VerifyOtpRequest(
    val phone: String,
    val otp: String
)

@Serializable
data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val sortOrder: Int = 0
)

@Serializable
data class Occasion(
    val id: String,
    val name: String,
    val slug: String,
    val description: String? = null,
    val bannerUrl: String? = null,
    val sortOrder: Int = 0
)

@Serializable
data class Banner(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String,
    val deepLink: String? = null,
    val position: String = "HOME_TOP",
    val sortOrder: Int = 0
)

@Serializable
data class HomeFeed(
    val banners: List<Banner> = emptyList(),
    val categories: List<Category> = emptyList(),
    val occasions: List<Occasion> = emptyList(),
    val sections: List<HomeSection> = emptyList()
)

@Serializable
data class HomeSection(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val products: List<Product> = emptyList()
)

@Serializable
data class Product(
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
    val averageRating: Double = 5.0,
    val reviewCount: Int = 0,
    val images: List<ProductImage> = emptyList(),
    val variants: List<ProductVariant> = emptyList(),
    val addOns: List<AddOn> = emptyList(),
    val reviews: List<ReviewModel> = emptyList()
)

@Serializable
data class ProductDetailResponse(
    val product: Product,
    val similarProducts: List<Product> = emptyList()
)

@Serializable
data class ProductVariant(
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
data class ProductImage(
    val id: String,
    val imageUrl: String,
    val isPrimary: Boolean = false,
    val sortOrder: Int = 0
)

@Serializable
data class AddOn(
    val id: String,
    val title: String,
    val description: String? = null,
    val price: Double,
    val imageUrl: String? = null,
    val stockQuantity: Int = 100,
    val isActive: Boolean = true
)

@Serializable
data class ReviewModel(
    val id: String,
    val rating: Int,
    val comment: String? = null,
    val createdAt: String,
    val user: ReviewUser? = null
)

@Serializable
data class ReviewUser(
    val id: String,
    val name: String? = null
)

@Serializable
data class PincodeCheckResponse(
    val isServiceable: Boolean,
    val code: String,
    val city: String? = null,
    val state: String? = null,
    val isSameDayAvailable: Boolean = false,
    val isMidnightAvailable: Boolean = false,
    val standardDeliveryFee: Double = 0.0,
    val midnightDeliveryFee: Double = 0.0,
    val message: String
)

@Serializable
data class DeliverySlot(
    val id: String,
    val title: String,
    val slotType: String,
    val startTime: String,
    val endTime: String,
    val fee: Double,
    val cutoffHoursBefore: Int = 2,
    val isAvailable: Boolean = true,
    val reason: String? = null
)

@Serializable
data class CartResponse(
    val cartId: String,
    val items: List<CartItemModel> = emptyList(),
    val itemCount: Int = 0,
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0
)

@Serializable
data class CartItemModel(
    val id: String,
    val productId: String,
    val productTitle: String,
    val productSlug: String,
    val productImage: String? = null,
    val variantId: String? = null,
    val variantTitle: String? = null,
    val unitPrice: Double,
    val quantity: Int,
    val personalizationText: String? = null,
    val deliveryDate: String? = null,
    val deliverySlot: DeliverySlot? = null,
    val addOns: List<CartItemAddOnModel> = emptyList(),
    val totalItemPrice: Double
)

@Serializable
data class CartItemAddOnModel(
    val addOnId: String,
    val title: String,
    val price: Double,
    val quantity: Int,
    val total: Double
)

@Serializable
data class AddToCartRequest(
    val productId: String,
    val variantId: String? = null,
    val quantity: Int = 1,
    val personalizationText: String? = null,
    val deliveryDate: String? = null,
    val deliverySlotId: String? = null,
    val addOns: List<AddOnRequest>? = null
)

@Serializable
data class AddOnRequest(
    val addOnId: String,
    val quantity: Int = 1
)

@Serializable
data class Address(
    val id: String,
    val name: String,
    val phone: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val landmark: String? = null,
    val city: String,
    val state: String,
    val pincode: String,
    val addressType: String = "HOME",
    val isDefault: Boolean = false
)

@Serializable
data class Coupon(
    val id: String,
    val code: String,
    val description: String? = null,
    val discountType: String,
    val discountValue: Double,
    val minOrderValue: Double = 0.0,
    val maxDiscountAmount: Double? = null,
    val validTo: String? = null
)

@Serializable
data class ValidateCouponRequest(
    val code: String,
    val cartSubtotal: Double
)

@Serializable
data class ValidateCouponResponse(
    val isValid: Boolean,
    val coupon: Coupon,
    val discountAmount: Double,
    val finalAmount: Double
)

@Serializable
data class InitiateCheckoutRequest(
    val addressId: String,
    val couponCode: String? = null,
    val paymentMethod: String = "RAZORPAY",
    val recipientName: String,
    val recipientPhone: String,
    val giftMessage: String? = null,
    val isSenderHidden: Boolean = false,
    val deliveryDate: String,
    val deliverySlotId: String
)

@Serializable
data class InitiateCheckoutResponse(
    val order: Order,
    val razorpay: RazorpayOrderData? = null,
    val paymentMethod: String? = null,
    val message: String? = null
)

@Serializable
data class RazorpayOrderData(
    val keyId: String,
    val orderId: String,
    val amount: Int,
    val currency: String,
    val name: String,
    val description: String? = null
)

@Serializable
data class VerifyPaymentRequest(
    val orderId: String,
    val razorpayOrderId: String,
    val razorpayPaymentId: String,
    val razorpaySignature: String
)

@Serializable
data class Order(
    val id: String,
    val orderNumber: String,
    val subtotal: Double,
    val deliveryFee: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
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
    val items: List<OrderItemModel> = emptyList(),
    val statusHistory: List<OrderStatusHistoryModel> = emptyList(),
    val createdAt: String
)

@Serializable
data class OrderItemModel(
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
data class OrderStatusHistoryModel(
    val id: String,
    val status: String,
    val notes: String? = null,
    val createdAt: String
)

@Serializable
data class Reminder(
    val id: String,
    val occasionTitle: String,
    val recipientName: String,
    val eventDate: String,
    val remindDaysBefore: Int = 2
)

@Serializable
data class CreateReminderRequest(
    val occasionTitle: String,
    val recipientName: String,
    val eventDate: String,
    val remindDaysBefore: Int = 2
)

@Serializable
data class NotificationModel(
    val id: String,
    val title: String,
    val body: String,
    val type: String = "ORDER_UPDATE",
    val isRead: Boolean = false,
    val createdAt: String
)
