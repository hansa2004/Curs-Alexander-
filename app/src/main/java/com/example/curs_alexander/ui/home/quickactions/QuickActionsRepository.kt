package com.example.curs_alexander.ui.home.quickactions

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.curs_alexander.settings.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Хранение набора быстрых действий (порядок, добавление/удаление).
 * Храним строку вида: "add_measurement,add_symptom,add_reminder".
 */
class QuickActionsRepository(private val context: Context) {

    private val appContext = context.applicationContext

    private val key = stringPreferencesKey(KEY_QUICK_ACTIONS)

    val quickActions: Flow<List<QuickActionType>> = appContext.settingsDataStore.data
        .map { prefs ->
            val raw = prefs[key]
            if (raw.isNullOrBlank()) defaultActions() else decode(raw)
        }

    val colors: Flow<Map<String, Int>> = appContext.settingsDataStore.data.map { prefs ->
        val result = mutableMapOf<String, Int>()
        // читаем только те действия, которые реально могут быть показаны (из текущего набора + дефолты)
        val ids = QuickActionType.entries.map { it.id }
        for (id in ids) {
            val v = prefs[intPreferencesKey(colorKey(id))]
            if (v != null) result[id] = v
        }
        result
    }

    val iconOverrides: Flow<Map<String, String>> = appContext.settingsDataStore.data.map { prefs ->
        val result = mutableMapOf<String, String>()
        val ids = QuickActionType.entries.map { it.id }
        for (id in ids) {
            val v = prefs[stringPreferencesKey(iconKey(id))]
            if (!v.isNullOrBlank()) result[id] = v
        }
        result
    }

    suspend fun setQuickActions(actions: List<QuickActionType>) {
        appContext.settingsDataStore.edit { prefs ->
            prefs[key] = actions.joinToString(",") { it.id }
        }
    }

    suspend fun setColor(actionId: String, argb: Int) {
        appContext.settingsDataStore.edit { prefs ->
            prefs[intPreferencesKey(colorKey(actionId))] = argb
        }
    }

    suspend fun clearColor(actionId: String) {
        appContext.settingsDataStore.edit { prefs ->
            prefs.remove(intPreferencesKey(colorKey(actionId)))
        }
    }

    suspend fun setIconStyle(actionId: String, styleId: String) {
        appContext.settingsDataStore.edit { prefs ->
            prefs[stringPreferencesKey(iconKey(actionId))] = styleId
        }
    }

    suspend fun clearIconStyle(actionId: String) {
        appContext.settingsDataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey(iconKey(actionId)))
        }
    }

    fun defaultActions(): List<QuickActionType> = listOf(
        QuickActionType.ADD_PRESSURE,
        QuickActionType.ADD_PULSE,
        QuickActionType.ADD_SYMPTOM,
        QuickActionType.ADD_REMINDER
    )

    private fun decode(raw: String): List<QuickActionType> {
        fun migrateId(id: String): String = when (id) {
            // старый общий "добавить показатель" делим на два основных варианта
            "add_measurement" -> QuickActionType.ADD_PRESSURE.id
            // старые переходы в разделы (дубли) удаляем
            "open_measurements", "open_symptoms", "open_reminders", "open_analytics", "open_medical_card", "open_settings" -> ""
            else -> id
        }

        val parsed = raw.split(',')
            .map { migrateId(it.trim()) }
            .filter { it.isNotBlank() }
            .mapNotNull { QuickActionType.fromId(it) }

        return if (parsed.isEmpty()) defaultActions() else parsed
    }

    private fun colorKey(actionId: String): String = "qa_color_${actionId}"
    private fun iconKey(actionId: String): String = "qa_icon_${actionId}"

    private companion object {
        private const val KEY_QUICK_ACTIONS = "quick_actions"
    }
}
