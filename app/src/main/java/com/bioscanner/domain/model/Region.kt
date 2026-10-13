package com.bioscanner.domain.model

/**
 * Região geográfica (hoje: estado brasileiro) com o contorno usado no mapa.
 *
 * Cada anel é um FloatArray intercalado `lon, lat, lon, lat, …` em vez de uma
 * lista de objetos: o mapa percorre ~5 mil pontos a cada desenho/toque.
 * Os anéis incluem buracos (ex.: o DF dentro de Goiás), por isso o teste de
 * ponto e o desenho usam a regra par-ímpar.
 */
class Region(
    val id: String,
    val name: String,
    val rings: List<FloatArray>,
) {
    /** Teste de ponto em polígono por ray casting (regra par-ímpar). */
    fun contains(lon: Float, lat: Float): Boolean {
        var inside = false
        for (ring in rings) {
            var j = ring.size - 2
            var i = 0
            while (i < ring.size) {
                val xi = ring[i]
                val yi = ring[i + 1]
                val xj = ring[j]
                val yj = ring[j + 1]
                if ((yi > lat) != (yj > lat) && lon < (xj - xi) * (lat - yi) / (yj - yi) + xi) {
                    inside = !inside
                }
                j = i
                i += 2
            }
        }
        return inside
    }
}

/** Espécie com registros de ocorrência numa região (dados do GBIF). */
data class RegionalSpecies(
    val species: Species,
    val occurrences: Int,
)

/** Regiões do mapa + de onde/quando os dados foram extraídos, para citar a fonte. */
class RegionCatalog(
    val regions: List<Region>,
    val source: String,
    val generatedAt: String,
)
