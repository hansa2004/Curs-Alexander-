package com.example.curs_alexander.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.curs_alexander.data.db.DbProvider
import com.example.curs_alexander.data.migrations.LegacyDataMigrator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class AnalyticsViewModel(app: Application) : AndroidViewModel(app) {

    private val db = DbProvider.get(app)
    private val repo = AnalyticsRepository(db.bloodPressureDao(), db.symptomDao())

    private val _state = MutableStateFlow(AnalyticsUiState(isLoading = true))
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = AnalyticsUiState(isLoading = true)

            // Подстрахуемся: если ещё остались данные в SharedPreferences, мигрируем их в Room
            LegacyDataMigrator(getApplication()).migrateIfNeeded(db)

            val pressures = repo.loadPressureAll().sortedByDescending { it.timestampMillis }
            val symptoms = repo.loadSymptomsAll().sortedByDescending { it.timestampMillis }

            val summary = buildPressureSummary(pressures)
            val stats = buildSymptomStats(symptoms)

            _state.value = AnalyticsUiState(
                pressureSummary = summary,
                pressureHistory = pressures,
                symptomStats = stats,
                symptomLast = symptoms.take(20),
                isLoading = false
            )
        }
    }

    private fun buildPressureSummary(all: List<com.example.curs_alexander.data.db.BloodPressureEntity>): PressureSummary {
        val now = Calendar.getInstance()
        val from = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
        val last7d = all.filter { it.timestampMillis >= from }

        val avgSys = last7d.takeIf { it.isNotEmpty() }?.map { it.systolic }?.average()?.toInt()
        val avgDia = last7d.takeIf { it.isNotEmpty() }?.map { it.diastolic }?.average()?.toInt()
        val last = all.firstOrNull()

        val high = last7d.any { it.systolic > 140 || it.diastolic > 90 }
        val hint = if (high) PressureHint.HIGH else PressureHint.NORMAL

        return PressureSummary(
            avgSystolic7d = avgSys,
            avgDiastolic7d = avgDia,
            last = last,
            hint = hint
        )
    }

    private fun buildSymptomStats(all: List<com.example.curs_alexander.data.db.SymptomEntity>): List<SymptomStats> {
        if (all.isEmpty()) return emptyList()

        val groups = all.groupBy { it.name.trim().lowercase(Locale.getDefault()) }
        return groups.entries
            .map { (name, list) ->
                val intensities = list.mapNotNull { it.intensity }
                val avg = intensities.takeIf { it.isNotEmpty() }?.average()
                SymptomStats(
                    name = name,
                    count = list.size,
                    avgIntensity = avg
                )
            }
            .sortedByDescending { it.count }
            .take(10)
    }
}

