package com.wishify.customer.data.repository

import com.wishify.customer.data.model.*
import com.wishify.customer.data.remote.NetworkClient
import com.wishify.customer.data.remote.WishifyCustomerApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WishifyRepository(
    private val api: WishifyCustomerApi = NetworkClient.api
) {
    suspend fun requestOtp(phone: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.requestOtp(RequestOtpRequest(phone))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.get("message") ?: "OTP sent")
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to send OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(phone: String, otp: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.verifyOtp(VerifyOtpRequest(phone, otp))
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "OTP verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHomeFeed(): Result<HomeFeed> = withContext(Dispatchers.IO) {
        try {
            val response = api.getHomeFeed()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to load home feed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(
        categoryId: String? = null,
        occasionId: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sort: String? = null
    ): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProducts(categoryId, occasionId, minPrice, maxPrice, sort = sort)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to load products"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductDetail(slug: String): Result<ProductDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProductDetail(slug)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Product not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkPincode(code: String): Result<PincodeCheckResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.checkPincode(code)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Invalid pincode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDeliverySlots(pincode: String?, date: String?): Result<List<DeliverySlot>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDeliverySlots(pincode, date)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to fetch slots"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCart(): Result<CartResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCart()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to fetch cart"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToCart(req: AddToCartRequest): Result<CartResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.addToCart(req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to add to cart"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeCartItem(itemId: String): Result<CartResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.removeCartItem(itemId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to remove item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCoupons(): Result<List<Coupon>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCoupons()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to fetch coupons"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun validateCoupon(code: String, amount: Double): Result<ValidateCouponResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.validateCoupon(ValidateCouponRequest(code, amount))
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Invalid coupon"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAddresses(): Result<List<Address>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAddresses()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to fetch addresses"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addAddress(address: Address): Result<Address> = withContext(Dispatchers.IO) {
        try {
            val response = api.addAddress(address)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to save address"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initiateCheckout(req: InitiateCheckoutRequest): Result<InitiateCheckoutResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.initiateCheckout(req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Checkout initiation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPayment(req: VerifyPaymentRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.verifyPayment(req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Payment verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrders(): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getOrders()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to fetch orders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderDetail(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = api.getOrderDetail(orderId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Order not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelOrder(orderId: String, reason: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = api.cancelOrder(orderId, mapOf("reason" to reason))
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Cancellation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWishlist(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getWishlist()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to fetch wishlist"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReminders(): Result<List<Reminder>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getReminders()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to fetch reminders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createReminder(req: CreateReminderRequest): Result<Reminder> = withContext(Dispatchers.IO) {
        try {
            val response = api.createReminder(req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to create reminder"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
