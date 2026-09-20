package com.bioscanner.domain.model

/**
 * Modelo de domínio de uma espécie. Não depende de Room nem de nenhum
 * framework Android, para manter a camada de domínio isolada.
 */
data class Species(
    val id: String,
    val scientificName: String,
    val popularName: String,
    val family: String,
    val category: String,
    val venomous: Boolean,
    val riskLevel: RiskLevel,
    val description: String,
    val geographicDistribution: String,
    val firstAidProcedure: String,
    val referenceImagePath: String?,
)
