package com.bioscanner.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {
    @Query("SELECT * FROM observation ORDER BY takenAt DESC")
    fun getAll(): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observation WHERE id = :id")
    fun getById(id: Long): Flow<ObservationEntity?>

    @Query("SELECT * FROM observation WHERE id = :id")
    suspend fun findById(id: Long): ObservationEntity?

    @Insert
    suspend fun insert(observation: ObservationEntity): Long

    @Query("DELETE FROM observation WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM observation")
    suspend fun deleteAll()
}
