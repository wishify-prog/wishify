package com.wishify.customer.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wishify.customer.data.remote.NetworkClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "wishify_user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_PHONE = stringPreferencesKey("user_phone")
        val KEY_SELECTED_PINCODE = stringPreferencesKey("selected_pincode")
    }

    val accessToken: Flow<String?> = context.userDataStore.data.map { prefs ->
        val token = prefs[KEY_ACCESS_TOKEN]
        NetworkClient.setAuthToken(token)
        token
    }

    val selectedPincode: Flow<String> = context.userDataStore.data.map { prefs ->
        prefs[KEY_SELECTED_PINCODE] ?: "560001"
    }

    val userPhone: Flow<String?> = context.userDataStore.data.map { prefs ->
        prefs[KEY_USER_PHONE]
    }

    suspend fun saveAuthTokens(accessToken: String, refreshToken: String, userId: String, phone: String?) {
        NetworkClient.setAuthToken(accessToken)
        context.userDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID] = userId
            phone?.let { prefs[KEY_USER_PHONE] = it }
        }
    }

    suspend fun savePincode(pincode: String) {
        context.userDataStore.edit { prefs ->
            prefs[KEY_SELECTED_PINCODE] = pincode
        }
    }

    suspend fun clearSession() {
        NetworkClient.setAuthToken(null)
        context.userDataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_PHONE)
        }
    }
}
