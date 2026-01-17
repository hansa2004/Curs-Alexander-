package com.example.curs_alexander.ui.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.curs_alexander.data.db.DbProvider
import com.example.curs_alexander.data.reminders.ReminderEntity
import com.example.curs_alexander.data.reminders.ReminderType
import com.example.curs_alexander.data.reminders.RemindersRepository
import com.example.curs_alexander.notifications.ReminderAlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(app: Application) : AndroidViewModel(app) {

    private val db = DbProvider.get(app)
    private val repo = RemindersRepository(db.reminderDao())

    val reminders: StateFlow<List<ReminderEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(type: ReminderType, hour: Int, minute: Int, enabled: Boolean) {
        viewModelScope.launch {
            val id = repo.add(type, hour, minute, enabled)
            if (enabled) {
                ReminderAlarmScheduler.scheduleDaily(getApplication(), id, hour, minute)
            }
        }
    }

    fun setEnabled(item: ReminderEntity, enabled: Boolean) {
        viewModelScope.launch {
            repo.setEnabled(item.id, enabled)
            if (enabled) {
                ReminderAlarmScheduler.scheduleDaily(getApplication(), item.id, item.hour, item.minute)
            } else {
                ReminderAlarmScheduler.cancel(getApplication(), item.id)
            }
        }
    }

    fun delete(item: ReminderEntity) {
        viewModelScope.launch {
            repo.delete(item.id)
            ReminderAlarmScheduler.cancel(getApplication(), item.id)
        }
    }
}

