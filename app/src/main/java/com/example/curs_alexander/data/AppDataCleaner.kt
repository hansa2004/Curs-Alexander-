package com.example.curs_alexander.data

import android.content.Context
import com.example.curs_alexander.data.db.DbProvider

/**
 * Единая точка очистки локальных данных приложения.
 *
 * Для диплома важно: логика удаления данных прозрачна и централизована,
 * а все операции выполняются локально в БД устройства.
 */
object AppDataCleaner {

    /**
     * Удаляет пользовательские данные:
     * - измерения (blood_pressure)
     * - симптомы (symptom)
     * - напоминания (reminder)
     * - локальные кеши в SharedPreferences (например, symptoms_diary)
     */
    fun clearAll(context: Context) {
        val db = DbProvider.get(context)
        db.runInTransaction {
            db.openHelper.writableDatabase.apply {
                delete("blood_pressure", null, null)
                delete("symptom", null, null)
                delete("reminder", null, null)
            }
        }

        // В проекте часть данных хранится в SharedPreferences (например, дневник симптомов).
        // Это тоже нужно чистить, иначе пользователь «не видит» очистку.
        runCatching {
            context.getSharedPreferences("symptoms_diary", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }

        // На будущее: если появятся другие prefs-хранилища — добавляем их здесь.
    }
}
