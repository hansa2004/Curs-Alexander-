package com.example.curs_alexander.settings

import android.content.Context

/**
 * Кэш настроек для раннего старта (до чтения DataStore).
 * Это помогает применить тему/шрифт без мигания при запуске.
 */
object SettingsCache {
    private const val PREFS = "settings_cache"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_FONT = "font_scale"

    fun getThemeMode(context: Context): ThemeMode? {
        val v = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_THEME, null)
        return when (v) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            "SYSTEM" -> ThemeMode.SYSTEM
            else -> null
        }
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, mode.name)
            .apply()
    }

    fun getFontScale(context: Context): FontScale? {
        val v = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_FONT, null)
        return when (v) {
            "SMALL" -> FontScale.SMALL
            "MEDIUM" -> FontScale.MEDIUM
            "LARGE" -> FontScale.LARGE
            else -> null
        }
    }

    fun setFontScale(context: Context, scale: FontScale) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FONT, scale.name)
            .apply()
    }
}

