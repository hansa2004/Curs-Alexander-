package com.example.curs_alexander.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pulse")
data class PulseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestampMillis: Long,
    val bpm: Int,
    val comment: String?
)

