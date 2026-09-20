package com.wishify.customer.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wishify.customer.data.model.Address
import com.wishify.customer.data.remote.NetworkClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "wishify_user_prefs")

class UserPreferences(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_PHONE = stringPreferencesKey("user_phone")
        val KEY_SELECTED_PINCODE = stringPreferencesKey("selected_pincode")
        val KEY_SAVED_ADDRESSES = stringPreferencesKey("saved_addresses")
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val token = context.userDataStore.data.map { it[KEY_ACCESS_TOKEN] }.firstOrNull()
            if (!token.isNullOrBlank()) {
                NetworkClient.setAuthToken(token)
            }
        }
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

    val savedAddresses: Flow<List<Address>> = context.userDataStore.data.map { prefs ->
        val raw = prefs[KEY_SAVED_ADDRESSES]
        if (!raw.isNullOrBlank()) {
            try {
                json.decodeFromString<List<Address>>(raw)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    suspend fun getSavedAddressesDirect(): List<Address> {
        val raw = context.userDataStore.data.map { it[KEY_SAVED_ADDRESSES] }.firstOrNull()
        return if (!raw.isNullOrBlank()) {
            try {
                json.decodeFromString<List<Address>>(raw)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    suspend fun saveSavedAddresses(addresses: List<Address>) {
        context.userDataStore.edit { prefs ->
            prefs[KEY_SAVED_ADDRESSES] = json.encodeToString(addresses)
        }
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
