package com.bioscanner.data.local.seed

import android.content.Context
import com.bioscanner.data.local.db.SpeciesEntity
import com.bioscanner.data.local.db.SpeciesRegionEntity
import kotlinx.serialization.json.Json
import java.io.BufferedReader

private const val SEED_ASSET_PATH = "seed/species_seed.json"

class SeedData(
    val version: String,
    val species: List<SpeciesEntity>,
    val regions: List<SpeciesRegionEntity>,
)

class SpeciesSeedLoader(private val context: Context) {

    fun load(): SeedData {
        val raw = context.assets.open(SEED_ASSET_PATH).bufferedReader().use(BufferedReader::readText)
        return parseSeed(raw)
    }
}

private val json = Json { ignoreUnknownKeys = true }

/** Separado do Context para poder ser testado na JVM. */
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
