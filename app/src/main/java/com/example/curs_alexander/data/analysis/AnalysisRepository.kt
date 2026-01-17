package com.example.curs_alexander.data.analysis

import com.example.curs_alexander.data.db.BloodPressureDao
import com.example.curs_alexander.data.db.SymptomDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnalysisRepository(
    private val bpDao: BloodPressureDao,
    private val symptomDao: SymptomDao
) {

    /**
     * Загружает данные за период (с fromMillis по текущий момент).
     * Выполняется на IO.
     */
    suspend fun loadPeriod(fromMillis: Long): PeriodData = withContext(Dispatchers.IO) {
        val bp = bpDao.getFrom(fromMillis)
        val symptoms = symptomDao.getFrom(fromMillis)
        PeriodData(bp = bp, symptoms = symptoms)
    }
}

