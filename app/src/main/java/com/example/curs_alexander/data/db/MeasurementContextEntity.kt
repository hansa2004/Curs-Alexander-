package com.example.curs_alexander.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Контекст измерения — только поясняющая информация.
 * Не содержит медицинской интерпретации.
 */
@Entity(
    tableName = "measurement_context",
    indices = [
        Index(value = ["measurementType", "measurementId"], unique = true)
    ]
)
data class MeasurementContextEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Тип измерения, к которому относится контекст (например: "bp", "pulse").
     * Без внешних ключей, т.к. в Room неудобно делать полиморфные FK.
     */
    val measurementType: String,

    /**
     * ID записи в таблице измерений (blood_pressure/pulse).
     */
    val measurementId: Long,

    /** утро/день/вечер или null (не указано) */
    val timeOfDay: String?,

    /** покой/после нагрузки/после стресса или null (не указано) */
    val state: String?,

    /** Дополнительный комментарий к контексту (необязательный) */
    val comment: String?
)

