package com.example.curs_alexander.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptom")
data class SymptomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestampMillis: Long,
    val name: String,
    val intensity: Int?,
    val comment: String?
)
