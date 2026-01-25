package com.example.curs_alexander.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PulseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PulseEntity): Long

    @Query("SELECT * FROM pulse ORDER BY timestampMillis DESC")
    suspend fun getAll(): List<PulseEntity>

    @Query("SELECT COUNT(*) FROM pulse")
    suspend fun count(): Int
}

