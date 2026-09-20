package com.bioscanner.data.local.seed

import android.content.Context
import com.bioscanner.data.local.db.SpeciesEntity
import kotlinx.serialization.json.Json
import java.io.BufferedReader

private const val SEED_ASSET_PATH = "seed/species_seed.json"

class SpeciesSeedLoader(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    fun loadSeedEntities(): List<SpeciesEntity> {
        val raw = context.assets.open(SEED_ASSET_PATH).bufferedReader().use(BufferedReader::readText)
        val dtos = json.decodeFromString<List<SpeciesSeedDto>>(raw)
        return dtos.map { it.toEntity() }
    }

    private fun SpeciesSeedDto.toEntity() = SpeciesEntity(
        id = id,
        scientificName = scientificName,
        popularName = popularName,
        family = family,
        category = category,
        venomous = venomous,
        riskLevel = riskLevel,
        description = description,
        geographicDistribution = geographicDistribution,
        firstAidProcedure = firstAidProcedure,
        referenceImagePath = referenceImagePath,
        classifierLabel = classifierLabel ?: id,
    )
}
