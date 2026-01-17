package com.example.curs_alexander.export

import com.example.curs_alexander.data.db.BloodPressureEntity
import com.example.curs_alexander.data.db.SymptomEntity

data class UserInfo(
    val name: String?,
    val birthDate: String?
)

data class PdfReportData(
    val title: String,
    val user: UserInfo,
    val periodText: String,
    val lastPressure: BloodPressureEntity?,
    val avgPressure7d: Pair<Int, Int>?,
    val pressures: List<BloodPressureEntity>,
    val symptoms: List<SymptomEntity>,
    val symptomStatsLines: List<String>,
    // Короткое резюме для быстрого просмотра
    val totalPressureCount: Int,
    val totalSymptomsCount: Int,
    val observationDays: Int?,
    val topSymptoms: List<String>
)
