package com.example.curs_alexander.ui.analytics

import com.example.curs_alexander.data.db.BloodPressureDao
import com.example.curs_alexander.data.db.BloodPressureWithContext
import com.example.curs_alexander.data.db.SymptomDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnalyticsRepository(
    private val bpDao: BloodPressureDao,
    private val symptomDao: SymptomDao
) {
    suspend fun loadPressureAll(): List<BloodPressureWithContext> =
        withContext(Dispatchers.IO) {
            bpDao.getAllWithContext()
        }

    suspend fun loadSymptomsAll(): List<com.example.curs_alexander.data.db.SymptomEntity> =
        withContext(Dispatchers.IO) {
            symptomDao.getFrom(0)
        }
}
