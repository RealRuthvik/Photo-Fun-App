package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val WINDOW_START_HOUR = longPreferencesKey("window_start_hour")
        val WINDOW_END_HOUR = longPreferencesKey("window_end_hour")
        val TODAY_DATE = androidx.datastore.preferences.core.stringPreferencesKey("today_date")
        val TODAY_PROMPT = androidx.datastore.preferences.core.stringPreferencesKey("today_prompt")
    }

    val todayDate: Flow<String?> = context.dataStore.data.map { it[TODAY_DATE] }
    val todayPrompt: Flow<String?> = context.dataStore.data.map { it[TODAY_PROMPT] }

    suspend fun setTodayPrompt(date: String, prompt: String) {
        context.dataStore.edit { preferences ->
            preferences[TODAY_DATE] = date
            preferences[TODAY_PROMPT] = prompt
        }
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }

    val windowStartHour: Flow<Int> = context.dataStore.data
        .map { preferences ->
            (preferences[WINDOW_START_HOUR] ?: 9L).toInt() // Default 9 AM
        }

    val windowEndHour: Flow<Int> = context.dataStore.data
        .map { preferences ->
            (preferences[WINDOW_END_HOUR] ?: 18L).toInt() // Default 6 PM
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setWindowStartHour(hour: Int) {
        context.dataStore.edit { preferences ->
            preferences[WINDOW_START_HOUR] = hour.toLong()
        }
    }

    suspend fun setWindowEndHour(hour: Int) {
        context.dataStore.edit { preferences ->
            preferences[WINDOW_END_HOUR] = hour.toLong()
        }
    }
}
