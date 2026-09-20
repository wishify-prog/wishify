package com.wishify.admin

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.wishify.admin.presentation.navigation.AdminNavHost
import com.wishify.admin.ui.theme.WishifyAdminTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WishifyAdminApplication

        setContent {
            WishifyAdminTheme {
                AdminNavHost(
                    repository = app.repository,
                    adminPreferences = app.adminPreferences
                )
            }
        }
    }
}
