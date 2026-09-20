package com.wishify.admin

import android.app.Application
import com.wishify.admin.data.local.AdminPreferences
import com.wishify.admin.data.repository.WishifyAdminRepository

class WishifyAdminApplication : Application() {

    lateinit var adminPreferences: AdminPreferences
        private set

    lateinit var repository: WishifyAdminRepository
        private set

    override fun onCreate() {
        super.onCreate()
        adminPreferences = AdminPreferences(this)
        repository = WishifyAdminRepository()
    }
}
