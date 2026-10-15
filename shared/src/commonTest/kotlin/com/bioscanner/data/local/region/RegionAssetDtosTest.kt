package com.bioscanner.data.local.region

import kotlinx.serialization.json.Json
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.Test

class RegionAssetDtosTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `region rings are flattened into interleaved lon-lat arrays`() {
        val file = json.decodeFromString<RegionFileDto>(
            """
            {"source":"GBIF.org e IBGE","generatedAt":"2026-10-05",
             "states":[{"uf":"MT","name":"Mato Grosso","rings":[[[-60.0,-10.0],[-55.5,-12.25]]]}]}
            """.trimIndent(),
        )
        val catalog = file.toDomain()
        val region = catalog.regions.single()

        assertEquals("2026-10-05", catalog.generatedAt)
        assertEquals("MT", region.id)
        assertEquals("Mato Grosso", region.name)
        assertContentEquals(floatArrayOf(-60f, -10f, -55.5f, -12.25f), region.rings.single())
    }
}
