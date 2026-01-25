package com.example.curs_alexander.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BloodPressureEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BloodPressureEntity>)

    @Query("SELECT * FROM blood_pressure ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<BloodPressureEntity>>

    @Query("SELECT * FROM blood_pressure WHERE timestampMillis >= :fromMillis ORDER BY timestampMillis DESC")
    suspend fun getFrom(fromMillis: Long): List<BloodPressureEntity>

    @Query("SELECT COUNT(*) FROM blood_pressure")
    suspend fun count(): Int

    @Query(
        """
        SELECT bp.*, 
               ctx.timeOfDay AS ctx_timeOfDay,
               ctx.state AS ctx_state,
               ctx.comment AS ctx_comment
        FROM blood_pressure bp
        LEFT JOIN measurement_context ctx
               ON ctx.measurementType = 'bp' AND ctx.measurementId = bp.id
        ORDER BY bp.timestampMillis DESC
        """
    )
    suspend fun getAllWithContext(): List<BloodPressureWithContext>

    @Query(
        """
        SELECT bp.*, 
               ctx.timeOfDay AS ctx_timeOfDay,
               ctx.state AS ctx_state,
               ctx.comment AS ctx_comment
        FROM blood_pressure bp
        LEFT JOIN measurement_context ctx
               ON ctx.measurementType = 'bp' AND ctx.measurementId = bp.id
        WHERE bp.timestampMillis >= :fromMillis
        ORDER BY bp.timestampMillis DESC
        """
    )
    suspend fun getFromWithContext(fromMillis: Long): List<BloodPressureWithContext>
}
