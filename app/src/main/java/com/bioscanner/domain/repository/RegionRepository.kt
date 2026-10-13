package com.bioscanner.domain.repository

import com.bioscanner.domain.model.RegionCatalog
import com.bioscanner.domain.model.RegionalSpecies
import kotlinx.coroutines.flow.Flow

/**
 * Contornos das regiões (estáticos, em assets) e a relação espécie × região
 * (no Room, junto das espécies).
 */
interface RegionRepository {
    suspend fun getRegionCatalog(): RegionCatalog
    fun observeSpeciesInRegion(regionId: String): Flow<List<RegionalSpecies>>
    fun observeRegionIdsForSpecies(speciesId: String): Flow<List<String>>
}
