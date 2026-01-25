package com.example.curs_alexander.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.curs_alexander.data.reminders.ReminderEntity
import com.example.curs_alexander.data.reminders.ReminderTypeConverters

@Database(
    entities = [
        BloodPressureEntity::class,
        PulseEntity::class,
        MeasurementContextEntity::class,
        SymptomEntity::class,
        ReminderEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(ReminderTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun pulseDao(): PulseDao
    abstract fun measurementContextDao(): MeasurementContextDao
    abstract fun symptomDao(): SymptomDao
    abstract fun reminderDao(): com.example.curs_alexander.data.reminders.ReminderDao
}
