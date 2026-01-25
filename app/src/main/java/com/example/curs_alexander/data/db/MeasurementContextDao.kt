package com.example.curs_alexander.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MeasurementContextDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MeasurementContextEntity): Long

    @Query(
        "SELECT * FROM measurement_context WHERE measurementType = :type AND measurementId = :id LIMIT 1"
    )
    suspend fun getFor(type: String, id: Long): MeasurementContextEntity?

    @Query(
        "SELECT * FROM measurement_context WHERE measurementType = :type AND measurementId IN (:ids)"
    )
    suspend fun getForMany(type: String, ids: List<Long>): List<MeasurementContextEntity>
}

