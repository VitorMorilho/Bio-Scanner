package com.bioscanner.data.repository

import android.content.Context
import com.bioscanner.data.local.db.SpeciesDao
import com.bioscanner.data.local.region.RegionFileDto
import com.bioscanner.data.local.region.toDomain
import com.bioscanner.domain.model.RegionCatalog
import com.bioscanner.domain.model.RegionalSpecies
import com.bioscanner.domain.repository.RegionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import javax.inject.Inject

private const val REGIONS_ASSET_PATH = "regions/br_states.json"

/**
 * Os contornos (~90 KB) são lidos uma vez dos assets e mantidos em memória:
 * geometria não combina com tabelas do Room. Já a relação espécie × região
 * vem do Room, para poder cruzar com os dados curados das espécies.
 */
class AssetRegionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: SpeciesDao,
) : RegionRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val catalog by lazy {
        val raw = context.assets.open(REGIONS_ASSET_PATH).bufferedReader().use(BufferedReader::readText)
        json.decodeFromString<RegionFileDto>(raw).toDomain()
    }

    override suspend fun getRegionCatalog(): RegionCatalog = withContext(Dispatchers.IO) { catalog }

    override fun observeSpeciesInRegion(regionId: String): Flow<List<RegionalSpecies>> =
        dao.getSpeciesInRegion(regionId).map { rows ->
            rows.map { RegionalSpecies(species = it.species.toDomain(), occurrences = it.occurrences) }
        }

    override fun observeRegionIdsForSpecies(speciesId: String): Flow<List<String>> =
        dao.getRegionIdsForSpecies(speciesId)
}
