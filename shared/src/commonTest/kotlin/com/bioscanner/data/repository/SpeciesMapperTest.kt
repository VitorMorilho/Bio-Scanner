package com.bioscanner.data.repository

import com.bioscanner.data.local.db.SpeciesEntity
import com.bioscanner.domain.model.RiskLevel
import kotlin.test.assertEquals
import kotlin.test.Test

class SpeciesMapperTest {

    private fun entity(riskLevel: String) = SpeciesEntity(
        id = "sp1",
        scientificName = "Species one",
        popularName = "Popular one",
        family = "Familia",
        category = "snake",
        venomous = true,
        riskLevel = riskLevel,
        description = "desc",
        geographicDistribution = "dist",
        firstAidProcedure = "procedure",
        referenceImagePath = null,
        classifierLabel = "sp1",
    )

    @Test
    fun `maps known risk level strings`() {
        assertEquals(RiskLevel.HIGH, entity("HIGH").toDomain().riskLevel)
        assertEquals(RiskLevel.MEDIUM, entity("MEDIUM").toDomain().riskLevel)
        assertEquals(RiskLevel.LOW, entity("LOW").toDomain().riskLevel)
    }

    @Test
    fun `unrecognized risk level string falls back to UNKNOWN`() {
        assertEquals(RiskLevel.UNKNOWN, entity("NOT_A_REAL_LEVEL").toDomain().riskLevel)
    }

    @Test
    fun `maps all other fields through unchanged`() {
        val domain = entity("LOW").toDomain()
        assertEquals("sp1", domain.id)
        assertEquals("Species one", domain.scientificName)
        assertEquals("Popular one", domain.popularName)
        assertEquals("Familia", domain.family)
        assertEquals("snake", domain.category)
        assertEquals(true, domain.venomous)
    }
}
