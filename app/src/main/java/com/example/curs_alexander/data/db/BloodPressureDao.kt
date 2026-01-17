package com.example.curs_alexander.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BloodPressureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BloodPressureEntity>)

    @Query("SELECT * FROM blood_pressure ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<BloodPressureEntity>>

    @Query("SELECT * FROM blood_pressure WHERE timestampMillis >= :fromMillis ORDER BY timestampMillis DESC")
    suspend fun getFrom(fromMillis: Long): List<BloodPressureEntity>

    @Query("SELECT COUNT(*) FROM blood_pressure")
    suspend fun count(): Int
}

