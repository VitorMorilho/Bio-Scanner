package com.bioscanner.data.repository

import com.bioscanner.data.local.db.SpeciesDao
import com.bioscanner.data.local.region.RegionFileDto
import com.bioscanner.data.local.region.toDomain
import com.bioscanner.data.platform.ResourceReader
import com.bioscanner.domain.model.RegionCatalog
import com.bioscanner.domain.model.RegionalSpecies
import com.bioscanner.domain.repository.RegionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

private const val REGIONS_ASSET_PATH = "regions/br_states.json"

/**
 * Os contornos (~90 KB) são lidos uma vez dos recursos do app e mantidos em
 * memória: geometria não combina com tabelas do Room. Já a relação espécie ×
 * região vem do Room, para poder cruzar com os dados curados das espécies.
 */
class AssetRegionRepository(
    private val resources: ResourceReader,
    private val dao: SpeciesDao,
) : RegionRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var catalog: RegionCatalog? = null

    override suspend fun getRegionCatalog(): RegionCatalog = mutex.withLock {
        catalog ?: json.decodeFromString<RegionFileDto>(resources.readText(REGIONS_ASSET_PATH))
            .toDomain()
            .also { catalog = it }
    }

    override fun observeSpeciesInRegion(regionId: String): Flow<List<RegionalSpecies>> =
        dao.getSpeciesInRegion(regionId).map { rows ->
            rows.map { RegionalSpecies(species = it.species.toDomain(), occurrences = it.occurrences) }
        }

    override fun observeRegionIdsForSpecies(speciesId: String): Flow<List<String>> =
        dao.getRegionIdsForSpecies(speciesId)
}
