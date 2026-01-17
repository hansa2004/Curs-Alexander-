package com.example.curs_alexander.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.curs_alexander.R
import com.example.curs_alexander.data.db.BloodPressureEntity
import com.example.curs_alexander.data.db.DbProvider
import com.example.curs_alexander.data.db.SymptomEntity
import com.example.curs_alexander.data.reminders.ReminderEntity
import com.example.curs_alexander.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

/**
 * Контекстные подсказки и карточка "Сегодня" для главного экрана.
 *
 * Требования:
 * - логика в ViewModel;
 * - без внешних API/интернета;
 * - подсказки нейтральные и информационные.
 */
class HomeTipsViewModel(app: Application) : AndroidViewModel(app) {

    data class TodayCardUi(
        val title: String,
        val message: String,
        val actionText: String?,
        val actionDestinationId: Int?
    )

    data class HintUi(
        val id: String,
        val message: String,
        val actionText: String?,
        val actionDestinationId: Int?
    )

    private val db = DbProvider.get(app)
    private val settingsRepo = SettingsRepository(app)

    private val measurementsFlow = db.bloodPressureDao().observeAll()
    private val symptomsFlow = db.symptomDao().observeAll()
    private val remindersFlow = db.reminderDao().observeAll()
    private val notificationsFlow = settingsRepo.notifications

    /**
     * Вспомогательный stream: ближайшее активное напоминание (по времени суток).
     * Мы не пытаемся вычислять точный AlarmManager trigger — только UX подсказку.
     */
    private val nextReminderFlow = combine(remindersFlow, notificationsFlow) { reminders, notif ->
        if (!notif.enabled) return@combine null
        val enabled = reminders.filter { it.enabled }
        if (enabled.isEmpty()) return@combine null
        pickNextReminder(enabled)
    }

    val todayCard: StateFlow<TodayCardUi> = combine(
        nextReminderFlow,
        notificationsFlow
    ) { nextReminder, notif ->
        val title = app.getString(R.string.home_today_title)
        when {
            !notif.enabled -> TodayCardUi(
                title = title,
                message = "Напоминания выключены. При желании включите их в настройках.",
                actionText = "Настройки",
                actionDestinationId = R.id.settingsFragment
            )

            nextReminder == null -> TodayCardUi(
                title = title,
                message = "Запланированных напоминаний нет. Можно настроить их в разделе \"Напоминания\".",
                actionText = "Настроить",
                actionDestinationId = R.id.remindersFragment
            )

            else -> {
                val time = String.format("%02d:%02d", nextReminder.hour, nextReminder.minute)
                val (text, dest) = when (nextReminder.type.name) {
                    "BLOOD_PRESSURE" -> "Измерить давление" to R.id.healthMeasurementAddFragment
                    else -> "Записать симптом" to R.id.symptomAddFragment
                }
                TodayCardUi(
                    title = title,
                    message = "Ближайшее: $time — $text",
                    actionText = "Открыть",
                    actionDestinationId = dest
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayCardUi(
        title = app.getString(R.string.home_today_title),
        message = app.getString(R.string.home_today_empty),
        actionText = null,
        actionDestinationId = null
    ))

    val hints: StateFlow<List<HintUi>> = combine(
        measurementsFlow,
        symptomsFlow
    ) { measurements, symptoms ->
        buildHints(app, measurements, symptoms)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun buildHints(
        app: Application,
        measurements: List<BloodPressureEntity>,
        symptoms: List<SymptomEntity>
    ): List<HintUi> {
        val now = System.currentTimeMillis()

        val result = mutableListOf<HintUi>()

        // 1) Измерения: если нет или давно не было
        val lastMeasurementMillis = measurements.maxOfOrNull { it.timestampMillis }
        val hoursSinceLast = lastMeasurementMillis?.let { (now - it) / (1000L * 60L * 60L) }
        if (lastMeasurementMillis == null || (hoursSinceLast != null && hoursSinceLast >= 48)) {
            result += HintUi(
                id = "need_measurement",
                message = "Рекомендуется внести новое измерение, чтобы история была актуальной.",
                actionText = "Добавить",
                actionDestinationId = R.id.healthMeasurementAddFragment
            )
        }

        // 2) Симптомы: если нет записей за последние дни
        val daysWindow = 5
        val fromMillis = now - daysWindow * 24L * 60L * 60L * 1000L
        val hasRecentSymptoms = symptoms.any { it.timestampMillis >= fromMillis }
        if (!hasRecentSymptoms) {
            result += HintUi(
                id = "no_symptoms",
                message = "Нет записей симптомов за последние дни. Если есть что отметить — можно добавить запись.",
                actionText = "Записать",
                actionDestinationId = R.id.symptomAddFragment
            )
        }

        // Ограничиваем шум: максимум 2 подсказки
        return result.take(2)
    }

    private fun pickNextReminder(list: List<ReminderEntity>): ReminderEntity {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val sorted = list.sortedWith(compareBy<ReminderEntity> { it.hour }.thenBy { it.minute })

        // Ищем ближайшее "сегодня", иначе берём первое "завтра"
        return sorted.firstOrNull { it.hour * 60 + it.minute > currentMinutes } ?: sorted.first()
    }
}

