package com.example.curs_alexander.ui.home.quickactions

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.curs_alexander.settings.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Хранение набора быстрых действий (порядок, добавление/удаление).
 * Храним строку вида: "add_measurement,add_symptom,add_reminder".
 */
class QuickActionsRepository(private val context: Context) {

    private val key = stringPreferencesKey(KEY_QUICK_ACTIONS)

    val quickActions: Flow<List<QuickActionType>> = context.settingsDataStore.data
        .map { prefs ->
            val raw = prefs[key]
            if (raw.isNullOrBlank()) defaultActions() else decode(raw)
        }

    suspend fun setQuickActions(actions: List<QuickActionType>) {
        context.settingsDataStore.edit { prefs ->
            prefs[key] = actions.joinToString(",") { it.id }
        }
    }

    fun defaultActions(): List<QuickActionType> = listOf(
        QuickActionType.ADD_MEASUREMENT,
        QuickActionType.ADD_SYMPTOM,
        QuickActionType.ADD_REMINDER
    )

    private fun decode(raw: String): List<QuickActionType> {
        val parsed = raw.split(',')
            .mapNotNull { QuickActionType.fromId(it.trim()) }

        return if (parsed.isEmpty()) defaultActions() else parsed
    }

    private companion object {
        private const val KEY_QUICK_ACTIONS = "quick_actions"
    }
}
