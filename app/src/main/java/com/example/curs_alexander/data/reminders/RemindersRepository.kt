package com.example.curs_alexander.data.reminders

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RemindersRepository(private val dao: ReminderDao) {
    fun observeAll(): Flow<List<ReminderEntity>> = dao.observeAll()

    suspend fun add(type: ReminderType, hour: Int, minute: Int, enabled: Boolean): Long =
        withContext(Dispatchers.IO) {
            dao.insert(
                ReminderEntity(
                    type = type,
                    hour = hour,
                    minute = minute,
                    enabled = enabled
                )
            )
        }

    suspend fun setEnabled(id: Long, enabled: Boolean) = withContext(Dispatchers.IO) {
        val entity = dao.getById(id) ?: return@withContext
        dao.update(entity.copy(enabled = enabled))
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }
}

