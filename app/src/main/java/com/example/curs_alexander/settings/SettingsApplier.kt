package com.example.curs_alexander.settings

import androidx.appcompat.app.AppCompatDelegate

object SettingsApplier {

    fun applyTheme(mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    fun fontScaleFactor(scale: FontScale): Float = when (scale) {
        FontScale.SMALL -> 0.90f
        FontScale.MEDIUM -> 1.00f
        FontScale.LARGE -> 1.15f
    }
}

