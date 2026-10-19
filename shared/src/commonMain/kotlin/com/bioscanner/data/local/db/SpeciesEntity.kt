package com.bioscanner.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Campos curados (diet, activityPeriod, dentition, defensiveBehavior,
 * description…) são opcionais: vêm de tools/data/especies.csv e ficam nulos
 * nas espécies que ainda não foram preenchidas — a UI esconde a seção.
 */
@Entity(tableName = "species")
data class SpeciesEntity(
    @PrimaryKey val id: String,
    val scientificName: String,
    val popularName: String?,
    val family: String,
    val category: String,
    val venomous: Boolean,
    val riskLevel: String,
    val description: String?,
    val geographicDistribution: String?,
    val firstAidProcedure: String?,
    val referenceImagePath: String?,
    val classifierLabel: String?,
    val gbifKey: Long? = null,
    /** Nomes populares alternativos separados por [OTHER_NAMES_SEPARATOR]. */
    val otherNames: String = "",
    val dentition: String? = null,
    val diet: String? = null,
    val activityPeriod: String? = null,
    val defensiveBehavior: String? = null,
    val imageCredit: String? = null,
    /**
     * Nomes de [com.bioscanner.domain.model.ColorPattern] separados por
     * [OTHER_NAMES_SEPARATOR]. O default no SQL tem que bater com o
     * ADD COLUMN de MIGRATION_3_4.
     */
    @ColumnInfo(defaultValue = "")
    val colorPatterns: String = "",
) {
    companion object {
        // Lista simples demais para uma tabela própria ou TypeConverter.
        const val OTHER_NAMES_SEPARATOR = "|"
    }
}
