package com.example.curs_alexander.ui.datastatus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.curs_alexander.data.db.BloodPressureEntity
import com.example.curs_alexander.data.db.DbProvider
import com.example.curs_alexander.data.db.SymptomEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

/**
 * Механика "Контроль полноты пользовательских данных".
 *
 * Считает регулярность и достаточность записей за последние 7 календарных дней
 * (включая сегодня). Не содержит медицинских выводов — оценивается только
 * наличие/регулярность данных.
 */
class DataStatusViewModel(app: Application) : AndroidViewModel(app) {

    enum class OverallStatus {
        ENOUGH,
        PARTIAL,
        NOT_ENOUGH
    }

    data class UiState(
        val daysWithData: Int,
        val percent: Int,
        val hasToday: Boolean,
        val hasLast3Days: Boolean,
        val hasLast7Days: Boolean,
        val hasGapMoreThan2Days: Boolean,
        val status: OverallStatus
    )

    private val db = DbProvider.get(app)

    private val measurementsFlow = db.bloodPressureDao().observeAll()
    private val symptomsFlow = db.symptomDao().observeAll()

    val state: StateFlow<UiState> = combine(measurementsFlow, symptomsFlow) { measurements, symptoms ->
        buildState(measurements, symptoms)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UiState(
            daysWithData = 0,
            percent = 0,
            hasToday = false,
            hasLast3Days = false,
            hasLast7Days = false,
            hasGapMoreThan2Days = false,
            status = OverallStatus.NOT_ENOUGH
        )
    )

    private fun buildState(
        measurements: List<BloodPressureEntity>,
        symptoms: List<SymptomEntity>
    ): UiState {
        val now = Calendar.getInstance()

        // Начало сегодняшнего дня (00:00)
        val startOfToday = now.toStartOfDayMillis()

        // Старт окна 7 дней: 6 дней назад в 00:00 (то есть 7 календарных дней включая сегодня)
        val startOfWindow = Calendar.getInstance().apply {
            timeInMillis = startOfToday
            add(Calendar.DAY_OF_YEAR, -6)
        }.timeInMillis

        // Нужны только записи в этом окне
        val inWindowMeasurements = measurements.filter { it.timestampMillis >= startOfWindow }
        val inWindowSymptoms = symptoms.filter { it.timestampMillis >= startOfWindow }

        // День считается "с данными", если есть хотя бы одна запись (измерение ИЛИ симптом)
        val daysWithDataSet = HashSet<Int>()
        inWindowMeasurements.forEach { daysWithDataSet += dayKey(it.timestampMillis) }
        inWindowSymptoms.forEach { daysWithDataSet += dayKey(it.timestampMillis) }

        // Последние 7 дней: ключи дней от startOfWindow..startOfToday
        val dayKeys = (0..6).map { offset ->
            Calendar.getInstance().apply {
                timeInMillis = startOfToday
                add(Calendar.DAY_OF_YEAR, -offset)
            }.let { dayKey(it.timeInMillis) }
        }

        val hasToday = dayKey(startOfToday) in daysWithDataSet

        // "Есть данные за последние 3 дня" = есть записи хотя бы в один из последних 3 календарных дней
        val last3DayKeys = dayKeys.take(3)
        val hasLast3Days = last3DayKeys.any { it in daysWithDataSet }

        // "Есть данные за последние 7 дней" = есть записи хотя бы в один из 7 дней
        val hasLast7Days = dayKeys.any { it in daysWithDataSet }

        // Пропуск > 2 дней подряд: 3+ подряд дней без записей внутри окна
        val hasGapMoreThan2Days = run {
            var streak = 0
            // Идём от старого к новому, чтобы корректно ловить подряд
            val ordered = dayKeys.asReversed()
            for (k in ordered) {
                if (k in daysWithDataSet) {
                    streak = 0
                } else {
                    streak++
                    if (streak >= 3) return@run true
                }
            }
            false
        }

        val daysWithData = daysWithDataSet.count { it in dayKeys }
        val percent = ((daysWithData / 7f) * 100f).toInt().coerceIn(0, 100)

        val status = when {
            daysWithData >= 6 -> OverallStatus.ENOUGH
            daysWithData >= 3 -> OverallStatus.PARTIAL
            else -> OverallStatus.NOT_ENOUGH
        }

        return UiState(
            daysWithData = daysWithData,
            percent = percent,
            hasToday = hasToday,
            hasLast3Days = hasLast3Days,
            hasLast7Days = hasLast7Days,
            hasGapMoreThan2Days = hasGapMoreThan2Days,
            status = status
        )
    }

    /**
     * Ключ дня без привязки к часовому поясу в строках: yyyyMMdd в int.
     * Нам достаточно устойчивого ключа для сравнения "дни совпадают".
     */
    private fun dayKey(timestampMillis: Long): Int {
        val c = Calendar.getInstance().apply { timeInMillis = timestampMillis }
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH) + 1
        val d = c.get(Calendar.DAY_OF_MONTH)
        return y * 10_000 + m * 100 + d
    }

    private fun Calendar.toStartOfDayMillis(): Long {
        val c = (this.clone() as Calendar)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}

