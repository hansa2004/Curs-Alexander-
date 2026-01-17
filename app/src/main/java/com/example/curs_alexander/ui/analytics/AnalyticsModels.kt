package com.example.curs_alexander.ui.analytics

import com.example.curs_alexander.data.db.BloodPressureEntity
import com.example.curs_alexander.data.db.SymptomEntity

enum class PressureHint {
    NORMAL,
    HIGH
}

data class PressureSummary(
    val avgSystolic7d: Int?,
    val avgDiastolic7d: Int?,
    val last: BloodPressureEntity?,
    val hint: PressureHint
)

data class SymptomStats(
    val name: String,
    val count: Int,
    val avgIntensity: Double?
)

data class AnalyticsUiState(
    val pressureSummary: PressureSummary? = null,
    val pressureHistory: List<BloodPressureEntity> = emptyList(),
    val symptomStats: List<SymptomStats> = emptyList(),
    val symptomLast: List<SymptomEntity> = emptyList(),
    val isLoading: Boolean = true
)

