package com.bioscanner.presentation.glossary.species

import com.bioscanner.domain.model.RiskLevel
import com.bioscanner.domain.model.Species
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeciesSearchTest {

    private val jararacucu = Species(
        id = "bothrops_jararacussu",
        scientificName = "Bothrops jararacussu",
        popularName = "Jararacuçu",
        family = "Viperidae",
        category = "snake",
        venomous = true,
        riskLevel = RiskLevel.HIGH,
        description = null,
        geographicDistribution = null,
        firstAidProcedure = null,
        referenceImagePath = null,
        otherNames = listOf("Surucucu-tapete"),
    )

    @Test
    fun `blank query matches everything`() {
        assertTrue(jararacucu.matches("  "))
    }

    @Test
    fun `ignores accents and case`() {
        assertTrue(jararacucu.matches("JARARACUCU"))
        assertTrue(jararacucu.matches("jararacuçu"))
    }

    @Test
    fun `matches scientific name, family and alternative names`() {
        assertTrue(jararacucu.matches("bothrops"))
        assertTrue(jararacucu.matches("viperidae"))
        assertTrue(jararacucu.matches("tapete"))
    }

    @Test
    fun `does not match unrelated text`() {
        assertFalse(jararacucu.matches("cascavel"))
    }
}
