package com.example.curs_alexander.data.migrations

import android.content.Context
import com.example.curs_alexander.data.HealthMeasurement
import com.example.curs_alexander.data.HealthMeasurementsStorage
import com.example.curs_alexander.data.db.AppDatabase
import com.example.curs_alexander.data.db.BloodPressureEntity
import com.example.curs_alexander.data.db.SymptomEntity
import com.example.curs_alexander.ui.symptoms.SymptomItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Одноразовая миграция старых данных (SharedPreferences/JSON) в Room.
 */
class LegacyDataMigrator(private val context: Context) {

    suspend fun migrateIfNeeded(db: AppDatabase) = withContext(Dispatchers.IO) {
        // Если миграция уже выполнялась (или была принудительно отключена) — ничего не делаем.
        val migPrefs = context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
        if (migPrefs.getBoolean(KEY_DONE, false)) return@withContext

        val bpCount = db.bloodPressureDao().count()
        val symptomCount = db.symptomDao().count()
        if (bpCount > 0 || symptomCount > 0) {
            migPrefs.edit().putBoolean(KEY_DONE, true).apply()
            return@withContext
        }

        // Миграция давления/пульса: берем только давление
        val legacyMeasurements = HealthMeasurementsStorage(context).getAll()
        val bpEntities = legacyMeasurements.mapNotNull { m ->
            when (m) {
                is HealthMeasurement.BloodPressure -> BloodPressureEntity(
                    timestampMillis = m.timestampMillis,
                    systolic = m.systolic,
                    diastolic = m.diastolic,
                    comment = m.comment
                )
                else -> null
            }
        }
        if (bpEntities.isNotEmpty()) {
            db.bloodPressureDao().insertAll(bpEntities)
        }

        // Миграция симптомов: формат как в SymptomAddFragment
        val prefs = context.getSharedPreferences("symptoms_diary", Context.MODE_PRIVATE)
        val jsonOld = prefs.getString("items", null)
        val legacySymptoms: List<SymptomItem> = if (jsonOld.isNullOrBlank()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<SymptomItem>>() {}.type
            runCatching { Gson().fromJson<List<SymptomItem>>(jsonOld, type) }.getOrNull() ?: emptyList()
        }

        val symptomEntities = legacySymptoms.map { s ->
            SymptomEntity(
                timestampMillis = s.timestampMillis,
                name = s.name,
                intensity = s.intensity,
                comment = s.comment
            )
        }
        if (symptomEntities.isNotEmpty()) {
            db.symptomDao().insertAll(symptomEntities)
        }

        // В конце помечаем, что миграция выполнена, чтобы не запускать её снова.
        migPrefs.edit().putBoolean(KEY_DONE, true).apply()
    }

    companion object {
        private const val MIGRATION_PREFS = "legacy_migration"
        private const val KEY_DONE = "done"

        /**
         * Вызывается при очистке данных: запрещает дальнейшую автозагрузку старых данных из prefs в Room.
         */
        fun markMigrationDone(context: Context) {
            context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DONE, true)
                .apply()
        }

        fun resetMigrationFlag(context: Context) {
            context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_DONE)
                .apply()
        }
    }
}
