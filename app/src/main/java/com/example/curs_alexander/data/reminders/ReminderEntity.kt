package com.example.curs_alexander.data.reminders

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: ReminderType,
    /** 0..23 */
    val hour: Int,
    /** 0..59 */
    val minute: Int,
    val enabled: Boolean
)

