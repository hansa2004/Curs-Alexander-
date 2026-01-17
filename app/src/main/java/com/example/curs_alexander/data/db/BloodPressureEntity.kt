package com.example.curs_alexander.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_pressure")
data class BloodPressureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestampMillis: Long,
    val systolic: Int,
    val diastolic: Int,
    val comment: String?
)

