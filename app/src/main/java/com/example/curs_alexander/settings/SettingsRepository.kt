package com.example.curs_alexander.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class FontScale { SMALL, MEDIUM, LARGE }

data class NotificationSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)

class SettingsRepository(private val context: Context) {

    val themeMode: Flow<ThemeMode> = context.settingsDataStore.data.map { prefs ->
        when (prefs[SettingsKeys.THEME_MODE]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    val fontScale: Flow<FontScale> = context.settingsDataStore.data.map { prefs ->
        when (prefs[SettingsKeys.FONT_SCALE]) {
            "SMALL" -> FontScale.SMALL
            "LARGE" -> FontScale.LARGE
            else -> FontScale.MEDIUM
        }
    }

    val notifications: Flow<NotificationSettings> = context.settingsDataStore.data.map { prefs ->
        NotificationSettings(
            enabled = prefs[SettingsKeys.REMINDERS_ENABLED] ?: true,
            hour = prefs[SettingsKeys.REMINDER_HOUR] ?: 9,
            minute = prefs[SettingsKeys.REMINDER_MINUTE] ?: 0
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        // Кэшируем сразу для раннего старта
        SettingsCache.setThemeMode(context, mode)
        context.settingsDataStore.edit { it[SettingsKeys.THEME_MODE] = mode.name }
    }

    suspend fun setFontScale(scale: FontScale) {
        // Кэшируем сразу для раннего старта
        SettingsCache.setFontScale(context, scale)
        context.settingsDataStore.edit { it[SettingsKeys.FONT_SCALE] = scale.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[SettingsKeys.REMINDERS_ENABLED] = enabled }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.settingsDataStore.edit {
            it[SettingsKeys.REMINDER_HOUR] = hour
            it[SettingsKeys.REMINDER_MINUTE] = minute
        }
    }
}
