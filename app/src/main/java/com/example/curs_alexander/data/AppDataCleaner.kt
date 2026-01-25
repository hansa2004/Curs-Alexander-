package com.example.curs_alexander.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.curs_alexander.data.db.DbProvider
import com.example.curs_alexander.data.migrations.LegacyDataMigrator
import com.example.curs_alexander.notifications.ReminderAlarmScheduler
import com.example.curs_alexander.settings.SettingsKeys
import com.example.curs_alexander.settings.settingsDataStore
import kotlinx.coroutines.runBlocking

/**
 * Единая точка очистки локальных данных приложения.
 *
 * Для диплома важно: логика удаления данных прозрачна и централизована,
 * а все операции выполняются локально в БД устройства.
 */
object AppDataCleaner {

    /**
     * Полностью очищает локальные пользовательские данные.
     *
     * Что чистим:
     * - Room (все таблицы)
     * - DataStore "settings" (все ключи)
     * - SharedPreferences (кэши/локальные экраны)
     * - системные Alarm-напоминания
     */
    fun clearAll(context: Context) {
        // 1) База (Room)
        val db = DbProvider.get(context)
        runCatching { db.clearAllTables() }

        // 2) SharedPreferences: локальные экраны/кэши/legacy
        // Важно: у вас есть SettingsCache (settings_cache), он иначе переживает очистку DataStore.
        // Плюс есть legacy-хранилища, из которых мигратор может восстановить данные.
        val prefsToClear = listOf(
            "symptoms_diary",
            "settings_cache",
            // legacy измерения (HealthMeasurementsStorage)
            "health_support_measurements"
        )
        prefsToClear.forEach { name ->
            runCatching {
                context.getSharedPreferences(name, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
            }
        }

        // 3) DataStore (settings)
        // Сначала чистим полностью, затем выставляем дефолты, чтобы UI сразу получил значения.
        runCatching {
            runBlocking {
                context.settingsDataStore.edit { prefs ->
                    prefs.clear()

                    // Дефолтные настройки (те же, что в SettingsRepository по умолчанию)
                    prefs[SettingsKeys.REMINDERS_ENABLED] = true
                    prefs[SettingsKeys.REMINDER_HOUR] = 9
                    prefs[SettingsKeys.REMINDER_MINUTE] = 0

                    // Чтобы тема/шрифт не зависели от старых значений
                    // (SettingsRepository трактует отсутствие ключей как SYSTEM и MEDIUM)
                    // Здесь можно не задавать явно, но это помогает избежать рассинхрона в UI.
                    // Оставляем ключи пустыми => получим SYSTEM/MEDIUM.
                    // prefs[SettingsKeys.THEME_MODE] = "SYSTEM"
                    // prefs[SettingsKeys.FONT_SCALE] = "MEDIUM"
                }
            }
        }

        // 4) Будильники/уведомления
        // Сначала отменяем, затем ставим заново по дефолту 09:00 (если включено).
        runCatching { ReminderAlarmScheduler.cancel(context, 1L) }
        runCatching { ReminderAlarmScheduler.scheduleDaily(context, 1L, 9, 0) }

        // Запрещаем повторную миграцию legacy-данных после ручной очистки.
        runCatching { LegacyDataMigrator.markMigrationDone(context) }
    }
}
