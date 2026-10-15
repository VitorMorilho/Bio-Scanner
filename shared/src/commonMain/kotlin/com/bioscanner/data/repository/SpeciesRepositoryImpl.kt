package com.bioscanner.data.repository

import com.bioscanner.data.local.db.SpeciesDao
import com.bioscanner.domain.model.Species
import com.bioscanner.domain.repository.SpeciesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SpeciesRepositoryImpl(
    private val dao: SpeciesDao,
) : SpeciesRepository {

    override fun observeAllSpecies(): Flow<List<Species>> =
        dao.getAllSpecies().map { list -> list.map { it.toDomain() } }

    override fun observeSpeciesById(id: String): Flow<Species?> =
        dao.getSpeciesById(id).map { it?.toDomain() }

    override fun observeSpeciesByCategory(category: String): Flow<List<Species>> =
        dao.getSpeciesByCategory(category).map { list -> list.map { it.toDomain() } }

    override suspend fun findByClassifierLabel(label: String): Species? =
        dao.findByClassifierLabel(label)?.toDomain()
}
