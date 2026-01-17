package com.example.curs_alexander.data.analysis

import com.example.curs_alexander.data.db.BloodPressureEntity
import com.example.curs_alexander.data.db.SymptomEntity

data class PeriodData(
    val bp: List<BloodPressureEntity>,
    val symptoms: List<SymptomEntity>
)

