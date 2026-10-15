package com.bioscanner.data.local.region

import com.bioscanner.domain.model.Region
import com.bioscanner.domain.model.RegionCatalog
import kotlinx.serialization.Serializable

/**
 * Espelha assets/regions/br_states.json, gerado por
 * tools/data/build_species_data.py — mudou o script, muda aqui também.
 */
@Serializable
data class RegionFileDto(
    val source: String,
    val generatedAt: String,
    val states: List<RegionDto>,
)

@Serializable
data class RegionDto(
    val uf: String,
    val name: String,
    val rings: List<List<List<Float>>>,
)

fun RegionFileDto.toDomain() = RegionCatalog(
    regions = states.map { it.toDomain() },
    source = source,
    generatedAt = generatedAt,
)

fun RegionDto.toDomain() = Region(
    id = uf,
    name = name,
    rings = rings.map { ring ->
        FloatArray(ring.size * 2) { i -> ring[i / 2][i % 2] }
    },
)
