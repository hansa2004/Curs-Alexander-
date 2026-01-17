package com.example.curs_alexander.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SymptomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SymptomEntity>)

    @Query("SELECT * FROM symptom ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<SymptomEntity>>

    @Query("SELECT * FROM symptom WHERE timestampMillis >= :fromMillis ORDER BY timestampMillis DESC")
    suspend fun getFrom(fromMillis: Long): List<SymptomEntity>

    @Query("SELECT COUNT(*) FROM symptom")
    suspend fun count(): Int
}

