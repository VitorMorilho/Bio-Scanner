package com.bioscanner.data.local.seed

import kotlinx.serialization.Serializable

/**
 * Espelha assets/seed/species_seed.json, gerado por
 * tools/data/build_species_data.py a partir de tools/data/especies.csv —
 * mudou o script, muda aqui também.
 */
@Serializable
data class SpeciesSeedFileDto(
    val version: String,
    val species: List<SpeciesSeedDto>,
)

@Serializable
data class SpeciesSeedDto(
    val id: String,
    val gbifKey: Long? = null,
    val scientificName: String,
    val popularName: String? = null,
    val otherNames: List<String> = emptyList(),
    val family: String,
    val category: String = "snake",
    val venomous: Boolean,
    val riskLevel: String = "UNKNOWN",
    val dentition: String? = null,
    val diet: String? = null,
    val activityPeriod: String? = null,
    val defensiveBehavior: String? = null,
    val description: String? = null,
    val geographicDistribution: String? = null,
    val firstAidProcedure: String? = null,
    val referenceImagePath: String? = null,
    val imageCredit: String? = null,
    val colorPatterns: List<String> = emptyList(),
    val classifierLabel: String? = null,
    val regions: List<SpeciesRegionSeedDto> = emptyList(),
)

@Serializable
data class SpeciesRegionSeedDto(
    val regionId: String,
    val occurrences: Int,
)
