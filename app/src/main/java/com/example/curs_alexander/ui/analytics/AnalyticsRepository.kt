package com.example.curs_alexander.ui.analytics

import com.example.curs_alexander.data.db.BloodPressureDao
import com.example.curs_alexander.data.db.SymptomDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnalyticsRepository(
    private val bpDao: BloodPressureDao,
    private val symptomDao: SymptomDao
) {
    suspend fun loadPressureAll(): List<com.example.curs_alexander.data.db.BloodPressureEntity> =
        withContext(Dispatchers.IO) {
            // В DAO сейчас есть только getFrom/observeAll; для истории удобнее взять observeAll не получится без collect.
            // Поэтому используем getFrom(0) как "всё".
            bpDao.getFrom(0)
        }

    suspend fun loadSymptomsAll(): List<com.example.curs_alexander.data.db.SymptomEntity> =
        withContext(Dispatchers.IO) {
            symptomDao.getFrom(0)
        }
}

