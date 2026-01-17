package com.example.curs_alexander.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Единый DataStore для настроек приложения.
 */
val Context.settingsDataStore by preferencesDataStore(name = "settings")

object SettingsKeys {
    val THEME_MODE: Preferences.Key<String> = androidx.datastore.preferences.core.stringPreferencesKey("theme_mode")
    val FONT_SCALE: Preferences.Key<String> = androidx.datastore.preferences.core.stringPreferencesKey("font_scale")

    val REMINDERS_ENABLED: Preferences.Key<Boolean> = androidx.datastore.preferences.core.booleanPreferencesKey("reminders_enabled")
    val REMINDER_HOUR: Preferences.Key<Int> = androidx.datastore.preferences.core.intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE: Preferences.Key<Int> = androidx.datastore.preferences.core.intPreferencesKey("reminder_minute")
}

