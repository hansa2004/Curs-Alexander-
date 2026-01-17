package com.example.curs_alexander.ui.analysis

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.curs_alexander.data.analysis.AnalysisRepository
import com.example.curs_alexander.data.db.DbProvider
import com.example.curs_alexander.data.migrations.LegacyDataMigrator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AnalysisViewModel(app: Application) : AndroidViewModel(app) {

    private val db = DbProvider.get(app)
    private val repo = AnalysisRepository(db.bloodPressureDao(), db.symptomDao())

    private val _state = MutableStateFlow(AnalysisUiState(isLoading = true, cards = emptyList()))
    val state: StateFlow<AnalysisUiState> = _state.asStateFlow()

    private val dateOnly = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    fun load() {
        viewModelScope.launch {
            _state.value = AnalysisUiState(isLoading = true)

            // 1) Одноразовая миграция из старых SharedPreferences (если БД пустая)
            LegacyDataMigrator(getApplication()).migrateIfNeeded(db)

            // 2) Анализ за последние 7 дней
            val fromMillis = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -7)
            }.timeInMillis

            val data = repo.loadPeriod(fromMillis)
            _state.value = AnalysisUiState(
                isLoading = false,
                cards = buildCards(data)
            )
        }
    }

    private fun buildCards(data: com.example.curs_alexander.data.analysis.PeriodData): List<AnalysisCard> {
        val result = mutableListOf<AnalysisCard>()

        val bpList = data.bp
        val symptomList = data.symptoms

        // Среднее давление за 7 дней
        if (bpList.isEmpty()) {
            result += AnalysisCard(
                title = "Среднее давление за 7 дней",
                body = "Нет данных за последние 7 дней. Добавьте несколько измерений, чтобы увидеть сводку."
            )
        } else {
            val avgSys = bpList.map { it.systolic }.average()
            val avgDia = bpList.map { it.diastolic }.average()
            result += AnalysisCard(
                title = "Среднее давление за 7 дней",
                body = "По ${bpList.size} измерениям: ~${avgSys.toInt()}/${avgDia.toInt()}."
            )
        }

        // Количество измерений выше условной нормы
        val highCount = bpList.count { it.systolic > 140 || it.diastolic > 90 }
        result += AnalysisCard(
            title = "Измерения выше условной нормы",
            body = "За 7 дней: $highCount из ${bpList.size} измерений были выше 140/90."
        )

        // Предупреждение (нейтрально)
        if (bpList.isNotEmpty()) {
            val ratio = highCount.toDouble() / bpList.size.toDouble()
            if (highCount >= 3 || ratio >= 0.5) {
                result += AnalysisCard(
                    title = "Наблюдение",
                    body = "В последние 7 дней повышенные значения встречаются довольно часто. Это учебная подсказка: можно продолжить наблюдение и сравнить с самочувствием."
                )
            } else {
                result += AnalysisCard(
                    title = "Наблюдение",
                    body = "В последние 7 дней повышенные значения встречаются нечасто. Это учебная сводка без медицинских выводов."
                )
            }
        }

        // Частые симптомы
        if (symptomList.isEmpty()) {
            result += AnalysisCard(
                title = "Частые симптомы",
                body = "Нет записей симптомов за последние 7 дней."
            )
        } else {
            val top = symptomList
                .groupingBy { it.name.trim().lowercase(Locale.getDefault()) }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(3)

            val text = top.joinToString(separator = ", ") { (name, cnt) -> "$name — $cnt" }
            result += AnalysisCard(
                title = "Частые симптомы",
                body = "Топ за 7 дней: $text."
            )
        }

        // Возможная связь по датам (без утверждений)
        val highDays = bpList
            .filter { it.systolic > 140 || it.diastolic > 90 }
            .map { dateOnly.format(Date(it.timestampMillis)) }
            .toSet()

        val symptomDays = symptomList
            .map { dateOnly.format(Date(it.timestampMillis)) }
            .toSet()

        val overlapDays = highDays.intersect(symptomDays).sorted()
        if (overlapDays.isEmpty()) {
            result += AnalysisCard(
                title = "Совпадения по датам",
                body = "За 7 дней не найдено дней, где одновременно были повышенные измерения и записи симптомов."
            )
        } else {
            val shown = overlapDays.take(5).joinToString(", ")
            val suffix = if (overlapDays.size > 5) " и ещё ${overlapDays.size - 5}…" else ""
            result += AnalysisCard(
                title = "Совпадения по датам",
                body = "В некоторые дни встречались и симптомы, и повышенные измерения: $shown$suffix. Это не означает причинно‑следственную связь, но может быть полезно для наблюдений."
            )
        }

        return result
    }
}

