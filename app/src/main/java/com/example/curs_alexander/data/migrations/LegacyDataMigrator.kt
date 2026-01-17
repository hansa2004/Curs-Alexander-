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
 * Нужна, чтобы в курсовом проекте переход на Room не "сломал" старые записи.
 */
class LegacyDataMigrator(private val context: Context) {

    suspend fun migrateIfNeeded(db: AppDatabase) = withContext(Dispatchers.IO) {
        val bpCount = db.bloodPressureDao().count()
        val symptomCount = db.symptomDao().count()
        if (bpCount > 0 || symptomCount > 0) return@withContext

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
    }
}

