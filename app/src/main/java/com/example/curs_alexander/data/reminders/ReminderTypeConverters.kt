package com.example.curs_alexander.data.reminders

import androidx.room.TypeConverter

class ReminderTypeConverters {
    @TypeConverter
    fun fromType(value: ReminderType): String = value.name

    @TypeConverter
    fun toType(value: String): ReminderType = runCatching { ReminderType.valueOf(value) }
        .getOrDefault(ReminderType.BLOOD_PRESSURE)
}

