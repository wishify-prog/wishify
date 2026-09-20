package com.wishify.admin.data.repository

import com.wishify.admin.data.model.*
import com.wishify.admin.data.remote.AdminNetworkClient
import com.wishify.admin.data.remote.WishifyAdminApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WishifyAdminRepository(
    private val api: WishifyAdminApi = AdminNetworkClient.api
) {
    suspend fun login(req: AdminLoginRequest): Result<AdminAuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDashboardStats(): Result<DashboardStats> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDashboardStats()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to load dashboard stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDashboardCharts(): Result<List<ChartDataPoint>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDashboardCharts()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to load charts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrders(status: String? = null, search: String? = null): Result<List<AdminOrder>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getOrders(status, search)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to load orders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderDetail(id: String): Result<AdminOrder> = withContext(Dispatchers.IO) {
        try {
            val response = api.getOrderDetail(id)
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

    suspend fun updateOrderStatus(id: String, status: String, notes: String? = null): Result<AdminOrder> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateOrderStatus(id, UpdateStatusRequest(status, notes))
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to update status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignDelivery(id: String, name: String, phone: String, tracking: String?): Result<AdminOrder> = withContext(Dispatchers.IO) {
        try {
            val response = api.assignDelivery(id, AssignDeliveryRequest(name, phone, tracking))
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to assign delivery"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refundOrder(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.refundOrder(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Refund failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(search: String? = null): Result<List<AdminProduct>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProducts(search)
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

    suspend fun toggleProductActive(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.toggleProductActive(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to toggle status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomers(): Result<List<CustomerSummary>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCustomers()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to load customers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReviews(status: String? = null): Result<List<AdminReview>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getReviews(status)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to load reviews"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReview(id: String, status: String): Result<AdminReview> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateReview(id, UpdateReviewRequest(status))
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to update review"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun broadcastPush(title: String, bodyText: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.broadcastPush(BroadcastPushRequest(title, bodyText))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to broadcast"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- PRODUCTS CRUD ---
    suspend fun createProduct(req: CreateProductRequest): Result<AdminProduct> = withContext(Dispatchers.IO) {
        try {
            val response = api.createProduct(req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to create product"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(id: String, req: UpdateProductRequest): Result<AdminProduct> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateProduct(id, req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to update product"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(id: String, permanent: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProduct(id, permanent)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to delete product"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- CATEGORIES ---
    suspend fun getCategories(): Result<List<AdminCategory>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCategories()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to load categories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCategory(req: CreateCategoryRequest): Result<AdminCategory> = withContext(Dispatchers.IO) {
        try {
            val response = api.createCategory(req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to create category"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategory(id: String, req: CreateCategoryRequest): Result<AdminCategory> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateCategory(id, req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to update category"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteCategory(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to delete category"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- OCCASIONS ---
    suspend fun getOccasions(): Result<List<AdminOccasion>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getOccasions()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to load occasions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOccasion(req: CreateOccasionRequest): Result<AdminOccasion> = withContext(Dispatchers.IO) {
        try {
            val response = api.createOccasion(req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to create occasion"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOccasion(id: String, req: CreateOccasionRequest): Result<AdminOccasion> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateOccasion(id, req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to update occasion"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteOccasion(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteOccasion(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to delete occasion"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- BANNERS ---
    suspend fun getBanners(): Result<List<AdminBanner>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getBanners()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to load banners"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBanner(req: CreateBannerRequest): Result<AdminBanner> = withContext(Dispatchers.IO) {
        try {
            val response = api.createBanner(req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to create banner"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBanner(id: String, req: CreateBannerRequest): Result<AdminBanner> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateBanner(id, req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to update banner"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBanner(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteBanner(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to delete banner"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- COUPONS ---
    suspend fun getCoupons(): Result<List<AdminCoupon>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCoupons()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to load coupons"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCoupon(req: CreateCouponRequest): Result<AdminCoupon> = withContext(Dispatchers.IO) {
        try {
            val response = api.createCoupon(req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to create coupon"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCoupon(id: String, req: CreateCouponRequest): Result<AdminCoupon> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateCoupon(id, req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to update coupon"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCoupon(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteCoupon(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to delete coupon"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- PINCODES ---
    suspend fun getPincodes(): Result<List<AdminPincode>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPincodes()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to load pincodes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPincode(req: CreatePincodeRequest): Result<AdminPincode> = withContext(Dispatchers.IO) {
        try {
            val response = api.createPincode(req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to save pincode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePincode(id: String, req: CreatePincodeRequest): Result<AdminPincode> = withContext(Dispatchers.IO) {
        try {
            val response = api.updatePincode(id, req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to update pincode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePincode(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deletePincode(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to delete pincode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- SLOTS ---
    suspend fun getSlots(): Result<List<AdminSlot>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getSlots()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to load slots"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSlot(req: CreateSlotRequest): Result<AdminSlot> = withContext(Dispatchers.IO) {
        try {
            val response = api.createSlot(req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to create slot"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSlot(id: String, req: CreateSlotRequest): Result<AdminSlot> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateSlot(id, req)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Failed to update slot"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSlot(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteSlot(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to delete slot"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
