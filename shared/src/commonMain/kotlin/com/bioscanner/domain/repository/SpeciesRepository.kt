package com.bioscanner.domain.repository

import com.bioscanner.domain.model.Species
import kotlinx.coroutines.flow.Flow

interface SpeciesRepository {
    fun observeAllSpecies(): Flow<List<Species>>
    fun observeSpeciesById(id: String): Flow<Species?>
    fun observeSpeciesByCategory(category: String): Flow<List<Species>>
    suspend fun findByClassifierLabel(label: String): Species?
}
