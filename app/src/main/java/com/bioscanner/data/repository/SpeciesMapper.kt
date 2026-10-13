package com.bioscanner.data.repository

import com.bioscanner.data.local.db.SpeciesEntity
import com.bioscanner.domain.model.RiskLevel
import com.bioscanner.domain.model.Species

fun SpeciesEntity.toDomain(): Species = Species(
    id = id,
    scientificName = scientificName,
    popularName = popularName,
    family = family,
    category = category,
    venomous = venomous,
    riskLevel = riskLevel.toRiskLevel(),
    description = description,
    geographicDistribution = geographicDistribution,
    firstAidProcedure = firstAidProcedure,
    referenceImagePath = referenceImagePath,
    otherNames = otherNames.split(SpeciesEntity.OTHER_NAMES_SEPARATOR).filter { it.isNotBlank() },
    dentition = dentition,
    diet = diet,
    activityPeriod = activityPeriod,
    defensiveBehavior = defensiveBehavior,
    imageCredit = imageCredit,
)

private fun String.toRiskLevel(): RiskLevel =
    RiskLevel.entries.find { it.name == this } ?: RiskLevel.UNKNOWN
