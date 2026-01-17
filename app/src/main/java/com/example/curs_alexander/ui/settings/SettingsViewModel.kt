package com.example.curs_alexander.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.curs_alexander.settings.FontScale
import com.example.curs_alexander.settings.SettingsCache
import com.example.curs_alexander.settings.SettingsRepository
import com.example.curs_alexander.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app)

    // Важно: initial value берём из кэша, чтобы UI всегда показывал последний выбор мгновенно.
    private val initialTheme = SettingsCache.getThemeMode(app) ?: ThemeMode.SYSTEM
    private val initialFont = SettingsCache.getFontScale(app) ?: FontScale.MEDIUM

    val themeMode: StateFlow<ThemeMode> = repo.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialTheme)

    val fontScale: StateFlow<FontScale> = repo.fontScale
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialFont)

    val notifications = repo.notifications
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.example.curs_alexander.settings.NotificationSettings(true, 9, 0))

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { repo.setThemeMode(mode) }
    }

    fun setFont(scale: FontScale) {
        viewModelScope.launch { repo.setFontScale(scale) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { repo.setNotificationsEnabled(enabled) }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch { repo.setNotificationTime(hour, minute) }
    }
}
