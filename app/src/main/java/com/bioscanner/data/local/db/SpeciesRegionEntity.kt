package com.bioscanner.data.local.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Relação N:N espécie × região (UF), com o número de registros do GBIF.
 * regionId não é FK: os contornos das regiões ficam em assets, não no Room.
 */
@Entity(
    tableName = "species_region",
    primaryKeys = ["speciesId", "regionId"],
    foreignKeys = [
        ForeignKey(
            entity = SpeciesEntity::class,
            parentColumns = ["id"],
            childColumns = ["speciesId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("regionId")],
)
data class SpeciesRegionEntity(
    val speciesId: String,
    val regionId: String,
    val occurrences: Int,
)

/** Resultado do JOIN species × species_region de uma região. */
data class SpeciesInRegion(
    @Embedded val species: SpeciesEntity,
    val occurrences: Int,
)
