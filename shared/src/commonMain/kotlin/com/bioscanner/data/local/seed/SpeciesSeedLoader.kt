package com.bioscanner.data.local.seed

import com.bioscanner.data.local.db.SpeciesEntity
import com.bioscanner.data.local.db.SpeciesRegionEntity
import com.bioscanner.data.platform.ResourceReader
import kotlinx.serialization.json.Json

private const val SEED_ASSET_PATH = "seed/species_seed.json"

class SeedData(
    val version: String,
    val species: List<SpeciesEntity>,
    val regions: List<SpeciesRegionEntity>,
)

class SpeciesSeedLoader(private val resources: ResourceReader) {

    suspend fun load(): SeedData = parseSeed(resources.readText(SEED_ASSET_PATH))
}

private val json = Json { ignoreUnknownKeys = true }

/** Separado da leitura do arquivo para poder ser testado sem recursos. */
fun parseSeed(raw: String): SeedData {
    val file = json.decodeFromString<SpeciesSeedFileDto>(raw)
    return SeedData(
        version = file.version,
        species = file.species.map { it.toEntity() },
        regions = file.species.flatMap { sp ->
            sp.regions.map { SpeciesRegionEntity(speciesId = sp.id, regionId = it.regionId, occurrences = it.occurrences) }
        },
    )
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
    gbifKey = gbifKey,
    otherNames = otherNames.joinToString(SpeciesEntity.OTHER_NAMES_SEPARATOR),
    dentition = dentition,
    diet = diet,
    activityPeriod = activityPeriod,
    defensiveBehavior = defensiveBehavior,
    imageCredit = imageCredit,
)
