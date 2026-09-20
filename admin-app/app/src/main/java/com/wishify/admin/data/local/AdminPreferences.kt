package com.wishify.admin.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wishify.admin.data.remote.AdminNetworkClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.adminDataStore: DataStore<Preferences> by preferencesDataStore(name = "wishify_admin_prefs")

class AdminPreferences(private val context: Context) {

    companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("admin_access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("admin_refresh_token")
        val KEY_ADMIN_EMAIL = stringPreferencesKey("admin_email")
        val KEY_ADMIN_NAME = stringPreferencesKey("admin_name")
        val KEY_ADMIN_ROLE = stringPreferencesKey("admin_role")
    }

    val accessToken: Flow<String?> = context.adminDataStore.data.map { prefs ->
        val token = prefs[KEY_ACCESS_TOKEN]
        AdminNetworkClient.setAuthToken(token)
        token
    }

    val adminEmail: Flow<String?> = context.adminDataStore.data.map { prefs ->
        prefs[KEY_ADMIN_EMAIL]
    }

    val adminName: Flow<String?> = context.adminDataStore.data.map { prefs ->
        prefs[KEY_ADMIN_NAME]
    }

    suspend fun saveAuthTokens(accessToken: String, refreshToken: String, email: String, name: String?, role: String) {
        AdminNetworkClient.setAuthToken(accessToken)
        context.adminDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_ADMIN_EMAIL] = email
            name?.let { prefs[KEY_ADMIN_NAME] = it }
            prefs[KEY_ADMIN_ROLE] = role
        }
    }

    suspend fun clearSession() {
        AdminNetworkClient.setAuthToken(null)
        context.adminDataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_ADMIN_EMAIL)
            prefs.remove(KEY_ADMIN_NAME)
            prefs.remove(KEY_ADMIN_ROLE)
        }
    }
}
