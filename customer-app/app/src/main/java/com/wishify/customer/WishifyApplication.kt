package com.wishify.customer

import android.app.Application
import com.wishify.customer.data.local.UserPreferences
import com.wishify.customer.data.repository.WishifyRepository

class WishifyApplication : Application() {

    lateinit var userPreferences: UserPreferences
        private set

    lateinit var repository: WishifyRepository
        private set

    override fun onCreate() {
        super.onCreate()
        userPreferences = UserPreferences(this)
        repository = WishifyRepository()
    }
}
