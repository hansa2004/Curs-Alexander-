package com.example.curs_alexander.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.curs_alexander.data.db.DbProvider
import com.example.curs_alexander.data.db.BloodPressureWithContext
import com.example.curs_alexander.data.migrations.LegacyDataMigrator
import com.example.curs_alexander.userparams.UserParamsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class AnalyticsViewModel(app: Application) : AndroidViewModel(app) {

    private val db = DbProvider.get(app)
    private val repo = AnalyticsRepository(db.bloodPressureDao(), db.symptomDao())
    private val userParamsRepo = UserParamsRepository(app)

    private val _state = MutableStateFlow(AnalyticsUiState(isLoading = true))
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = AnalyticsUiState(isLoading = true)

            // Подстрахуемся: если ещё остались данные в SharedPreferences, мигрируем их в Room
            LegacyDataMigrator(getApplication()).migrateIfNeeded(db)

            val pressuresWithCtx = repo.loadPressureAll()
            val symptoms = repo.loadSymptomsAll().sortedByDescending { it.timestampMillis }

            val userParams = userParamsRepo.params.first()

            val summary = buildPressureSummary(pressuresWithCtx, userParams)
            val stats = buildSymptomStats(symptoms)

            _state.value = AnalyticsUiState(
                pressureSummary = summary,
                pressureHistory = pressuresWithCtx,
                symptomStats = stats,
                symptomLast = symptoms.take(20),
                isLoading = false
            )
        }
    }

    private fun buildPressureSummary(
        all: List<BloodPressureWithContext>,
        userParams: com.example.curs_alexander.userparams.UserParams
    ): PressureSummary {
        val from = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
        val last7d = all.map { it.bp }.filter { it.timestampMillis >= from }

        val avgSys = last7d.takeIf { it.isNotEmpty() }?.map { it.systolic }?.average()?.toInt()
        val avgDia = last7d.takeIf { it.isNotEmpty() }?.map { it.diastolic }?.average()?.toInt()
        val last = all.firstOrNull()?.bp

        val upperSys = userParams.upperSystolic
        val upperDia = userParams.upperDiastolic
        val lowerSys = userParams.lowerSystolic
        val lowerDia = userParams.lowerDiastolic

        val anyAbove = last7d.any { it.systolic > upperSys || it.diastolic > upperDia }
        val anyBelow = last7d.any { it.systolic < lowerSys || it.diastolic < lowerDia }

        val hint = when {
            anyAbove -> PressureHint.ABOVE_USER_THRESHOLD
            anyBelow -> PressureHint.BELOW_USER_THRESHOLD
            else -> PressureHint.WITHIN_USER_THRESHOLDS
        }

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
