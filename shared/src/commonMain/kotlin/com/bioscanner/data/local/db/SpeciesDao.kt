package com.bioscanner.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeciesDao {
    @Query("SELECT * FROM species ORDER BY COALESCE(popularName, scientificName) ASC")
    fun getAllSpecies(): Flow<List<SpeciesEntity>>

    @Query("SELECT * FROM species WHERE id = :id")
    fun getSpeciesById(id: String): Flow<SpeciesEntity?>

    @Query("SELECT * FROM species WHERE category = :category ORDER BY COALESCE(popularName, scientificName) ASC")
    fun getSpeciesByCategory(category: String): Flow<List<SpeciesEntity>>

    @Query("SELECT * FROM species WHERE classifierLabel = :label LIMIT 1")
    suspend fun findByClassifierLabel(label: String): SpeciesEntity?

    @Query(
        """
        SELECT species.*, species_region.occurrences AS occurrences
        FROM species JOIN species_region ON species.id = species_region.speciesId
        WHERE species_region.regionId = :regionId
        ORDER BY species_region.occurrences DESC
        """,
    )
    fun getSpeciesInRegion(regionId: String): Flow<List<SpeciesInRegion>>

    @Query("SELECT regionId FROM species_region WHERE speciesId = :speciesId ORDER BY occurrences DESC")
    fun getRegionIdsForSpecies(speciesId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(species: List<SpeciesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegions(regions: List<SpeciesRegionEntity>)

    @Query("DELETE FROM species_region")
    suspend fun deleteAllRegions()

    @Query("DELETE FROM species")
    suspend fun deleteAll()

    /** Troca todo o conteúdo pelo seed novo de uma vez (sem estado intermediário na UI). */
    @Transaction
    suspend fun replaceAll(species: List<SpeciesEntity>, regions: List<SpeciesRegionEntity>) {
        deleteAllRegions()
        deleteAll()
        insertAll(species)
        insertRegions(regions)
    }

    @Query("SELECT COUNT(*) FROM species")
    suspend fun count(): Int
}
