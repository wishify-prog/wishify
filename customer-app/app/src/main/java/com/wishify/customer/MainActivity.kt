package com.wishify.customer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.wishify.customer.data.remote.NetworkClient
import com.wishify.customer.data.remote.RealtimeSyncManager
import com.wishify.customer.presentation.navigation.WishifyNavHost
import com.wishify.customer.ui.theme.WishifyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WishifyApplication

        // Observe auth token changes and keep NetworkClient updated
        lifecycleScope.launch {
            app.userPreferences.accessToken.collect { token ->
                NetworkClient.setAuthToken(token)
            }
        }

        // Start SSE real-time listener for catalog & order updates
        lifecycleScope.launch {
            RealtimeSyncManager.startListening()
        }

        setContent {
            WishifyTheme {
                WishifyNavHost(
                    repository = app.repository,
                    userPreferences = app.userPreferences
                )
            }
        }
    }
}
