package com.example.curs_alexander.data.db

import androidx.room.Embedded
import androidx.room.ColumnInfo

/**
 * DTO для чтения давления вместе с контекстом (LEFT JOIN).
 */
data class BloodPressureWithContext(
    @Embedded val bp: BloodPressureEntity,

    @ColumnInfo(name = "ctx_timeOfDay") val timeOfDay: String?,
    @ColumnInfo(name = "ctx_state") val state: String?,
    @ColumnInfo(name = "ctx_comment") val contextComment: String?
)

