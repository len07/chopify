package com.example.chopify.models

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class User(
    val userID: String = "",
    val email: String = "",
    val password: String = "",
    val name: String = ""
)

// DataStore instance for Notifications
private val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(context: Context) {

    private val dataStore = context.dataStore

    // Key for expiry day notifications
    companion object {
        private val NOTIFICATION_EXPIRY_DAYS_KEY = intPreferencesKey("notification_expiry_days")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    }

    val selectedDaysFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[NOTIFICATION_EXPIRY_DAYS_KEY] ?: 3 // Default to 3 days
    }
    val notificationsEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED_KEY] ?: true // Default to true
    }

    // Save
    suspend fun saveSelectedDays(days: Int) {
        dataStore.edit { prefs ->
            prefs[NOTIFICATION_EXPIRY_DAYS_KEY] = days
        }
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
}
