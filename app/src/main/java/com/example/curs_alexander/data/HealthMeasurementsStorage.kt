package com.example.curs_alexander.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Простое локальное хранилище показателей здоровья на базе SharedPreferences.
 * Не использует сеть, не делает никаких медицинских выводов.
 */
class HealthMeasurementsStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(): List<HealthMeasurement> {
        val json = prefs.getString(KEY_MEASUREMENTS_JSON, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val result = mutableListOf<HealthMeasurement>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(HealthMeasurement.fromJson(obj))
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun add(measurement: HealthMeasurement) {
        val list = getAll().toMutableList()
        list.add(0, measurement) // новые сверху
        val array = JSONArray()
        list.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_MEASUREMENTS_JSON, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "health_support_measurements"
        private const val KEY_MEASUREMENTS_JSON = "healthMeasurementsJson"
    }
}

/**
 * Модель одного показателя здоровья.
 */
sealed class HealthMeasurement {
    abstract val type: Type
    abstract val timestampMillis: Long
    abstract val comment: String?

    enum class Type { BLOOD_PRESSURE, PULSE }

    data class BloodPressure(
        val systolic: Int,
        val diastolic: Int,
        override val timestampMillis: Long,
        override val comment: String?
    ) : HealthMeasurement() {
        override val type: Type = Type.BLOOD_PRESSURE
    }

    data class Pulse(
        val bpm: Int,
        override val timestampMillis: Long,
        override val comment: String?
    ) : HealthMeasurement() {
        override val type: Type = Type.PULSE
    }

    fun toJson(): JSONObject {
        val obj = JSONObject()
        when (this) {
            is BloodPressure -> {
                obj.put("kind", "bp")
                obj.put("systolic", systolic)
                obj.put("diastolic", diastolic)
                obj.put("timestamp", timestampMillis)
                obj.put("comment", comment)
            }
            is Pulse -> {
                obj.put("kind", "pulse")
                obj.put("bpm", bpm)
                obj.put("timestamp", timestampMillis)
                obj.put("comment", comment)
            }
        }
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): HealthMeasurement {
            return when (obj.optString("kind")) {
                "bp" -> BloodPressure(
                    systolic = obj.optInt("systolic"),
                    diastolic = obj.optInt("diastolic"),
                    timestampMillis = obj.optLong("timestamp"),
                    comment = obj.opt("comment") as? String
                )

                "pulse" -> Pulse(
                    bpm = obj.optInt("bpm"),
                    timestampMillis = obj.optLong("timestamp"),
                    comment = obj.opt("comment") as? String
                )

                else -> Pulse(
                    bpm = 0,
                    timestampMillis = obj.optLong("timestamp"),
                    comment = obj.opt("comment") as? String
                )
            }
        }
    }
}
