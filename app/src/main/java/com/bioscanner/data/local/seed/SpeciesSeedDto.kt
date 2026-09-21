package com.bioscanner.data.local.seed

import kotlinx.serialization.Serializable

/**
 * Espelha exatamente o schema de assets/seed/species_seed.json. Os nomes de
 * campo aqui devem bater 1:1 com as colunas de species_metadata usadas no
 * pipeline de treinamento externo (fora deste repositório), para que o
 * script de exportação seja um simples recorte de campos.
 */
@Serializable
data class SpeciesSeedDto(
    val id: String,
    val scientificName: String,
    val popularName: String,
    val family: String,
    val category: String = "snake",
    val venomous: Boolean,
    val riskLevel: String,
    val description: String,
    val geographicDistribution: String,
    val firstAidProcedure: String,
    val referenceImagePath: String? = null,
    val classifierLabel: String? = null,
)
