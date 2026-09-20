package com.vegam.budgetcalculator.core.common

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "budget_settings")

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode") // LIGHT, DARK, SYSTEM
        val CURRENCY = stringPreferencesKey("currency") // INR, USD, etc.
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "SYSTEM"
    }

    val currencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY] ?: "INR"
    }

    val syncEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SYNC_ENABLED] ?: false
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY] = currency
        }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_ENABLED] = enabled
        }
    }
}
