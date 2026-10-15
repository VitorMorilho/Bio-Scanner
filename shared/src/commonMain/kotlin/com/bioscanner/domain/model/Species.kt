package com.bioscanner.domain.model

/**
 * Modelo de domínio de uma espécie. Não depende de Room nem de nenhum
 * framework Android, para manter a camada de domínio isolada.
 *
 * Os campos curados são opcionais: só as espécies já preenchidas em
 * tools/data/especies.csv os têm.
 */
data class Species(
    val id: String,
    val scientificName: String,
    val popularName: String?,
    val family: String,
    val category: String,
    val venomous: Boolean,
    val riskLevel: RiskLevel,
    val description: String?,
    val geographicDistribution: String?,
    val firstAidProcedure: String?,
    val referenceImagePath: String?,
    val otherNames: List<String> = emptyList(),
    val dentition: String? = null,
    val diet: String? = null,
    val activityPeriod: String? = null,
    val defensiveBehavior: String? = null,
    val imageCredit: String? = null,
) {
    /** Nome para títulos: o popular, ou o científico quando não há um confiável. */
    val displayName: String get() = popularName ?: scientificName
}
