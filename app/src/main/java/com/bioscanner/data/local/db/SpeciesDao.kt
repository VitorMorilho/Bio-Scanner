package com.bioscanner.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeciesDao {
    @Query("SELECT * FROM species ORDER BY popularName ASC")
    fun getAllSpecies(): Flow<List<SpeciesEntity>>

    @Query("SELECT * FROM species WHERE id = :id")
    fun getSpeciesById(id: String): Flow<SpeciesEntity?>

    @Query("SELECT * FROM species WHERE category = :category ORDER BY popularName ASC")
    fun getSpeciesByCategory(category: String): Flow<List<SpeciesEntity>>

    @Query("SELECT * FROM species WHERE classifierLabel = :label LIMIT 1")
    suspend fun findByClassifierLabel(label: String): SpeciesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(species: List<SpeciesEntity>)

    @Query("SELECT COUNT(*) FROM species")
    suspend fun count(): Int
}
