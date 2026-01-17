package com.example.curs_alexander.export

import android.content.Context
import com.example.curs_alexander.data.Prefs
import com.example.curs_alexander.data.db.DbProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

class PdfReportRepository(private val context: Context) {

    private val df = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    suspend fun loadReportData(): PdfReportData = withContext(Dispatchers.IO) {
        val db = DbProvider.get(context)
        val prefs = Prefs(context)

        val fullName = buildString {
            val first = prefs.userName?.trim().orEmpty()
            val last = prefs.userLastName?.trim().orEmpty()
            if (first.isNotBlank()) append(first)
            if (last.isNotBlank()) {
                if (isNotEmpty()) append(' ')
                append(last)
            }
        }.ifBlank { null }

        val user = UserInfo(
            name = fullName,
            birthDate = prefs.userBirthDate
        )

        val pressuresAll = db.bloodPressureDao().getFrom(0).sortedByDescending { it.timestampMillis }
        val symptomsAll = db.symptomDao().getFrom(0).sortedByDescending { it.timestampMillis }

        val periodText = buildPeriodText(pressuresAll, symptomsAll)

        val lastPressure = pressuresAll.firstOrNull()

        val from7d = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
        val last7d = pressuresAll.filter { it.timestampMillis >= from7d }
        val avg7d = if (last7d.isNotEmpty()) {
            Pair(last7d.map { it.systolic }.average().toInt(), last7d.map { it.diastolic }.average().toInt())
        } else null

        val symptomStatsLines = symptomsAll
            .groupBy { it.name.trim().lowercase(Locale.getDefault()) }
            .entries
            .sortedByDescending { it.value.size }
            .map { (name, list) ->
                val intensities = list.mapNotNull { it.intensity }
                val avgInt = intensities.takeIf { it.isNotEmpty() }?.average()
                if (avgInt != null) {
                    "$name — ${list.size} раз(а), средняя интенсивность: ${String.format(Locale.getDefault(), "%.1f", avgInt)}"
                } else {
                    "$name — ${list.size} раз(а)"
                }
            }
            .take(20)

        val (minTs, maxTs) = findMinMaxTs(pressuresAll, symptomsAll)
        val observationDays = if (minTs != null && maxTs != null) {
            // +1 день, чтобы 17.01—17.01 = 1 день
            max(1, (((maxTs - minTs) / (24L * 60L * 60L * 1000L)).toInt() + 1))
        } else null

        val topSymptoms = symptomsAll
            .groupBy { it.name.trim().lowercase(Locale.getDefault()) }
            .entries
            .sortedByDescending { it.value.size }
            .take(3)
            .map { e -> "${e.key} (${e.value.size})" }

        PdfReportData(
            title = "Отчёт о самоконтроле состояния здоровья",
            user = user,
            periodText = periodText,
            lastPressure = lastPressure,
            avgPressure7d = avg7d,
            pressures = pressuresAll.take(200),
            symptoms = symptomsAll.take(200),
            symptomStatsLines = symptomStatsLines,
            totalPressureCount = pressuresAll.size,
            totalSymptomsCount = symptomsAll.size,
            observationDays = observationDays,
            topSymptoms = topSymptoms
        )
    }

    private fun findMinMaxTs(
        pressures: List<com.example.curs_alexander.data.db.BloodPressureEntity>,
        symptoms: List<com.example.curs_alexander.data.db.SymptomEntity>
    ): Pair<Long?, Long?> {
        val allTimes = buildList {
            addAll(pressures.map { it.timestampMillis })
            addAll(symptoms.map { it.timestampMillis })
        }
        if (allTimes.isEmpty()) return null to null
        return allTimes.minOrNull() to allTimes.maxOrNull()
    }

    private fun buildPeriodText(
        pressures: List<com.example.curs_alexander.data.db.BloodPressureEntity>,
        symptoms: List<com.example.curs_alexander.data.db.SymptomEntity>
    ): String {
        val (min, max) = findMinMaxTs(pressures, symptoms)
        if (min == null || max == null) return "Период: нет данных"
        return "Период: ${df.format(Date(min))} — ${df.format(Date(max))}"
    }
}
