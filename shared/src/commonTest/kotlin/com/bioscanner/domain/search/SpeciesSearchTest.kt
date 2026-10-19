package com.bioscanner.domain.search

import com.bioscanner.domain.model.ColorPattern
import com.bioscanner.domain.model.RiskLevel
import com.bioscanner.domain.model.Species
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

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
        colorPatterns = setOf(ColorPattern.VARIEGATED),
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
    fun `matches scientific name family and alternative names`() {
        assertTrue(jararacucu.matches("bothrops"))
        assertTrue(jararacucu.matches("viperidae"))
        assertTrue(jararacucu.matches("tapete"))
    }

    @Test
    fun `does not match unrelated text`() {
        assertFalse(jararacucu.matches("cascavel"))
    }

    @Test
    fun `no selected pattern matches everything`() {
        assertTrue(jararacucu.hasAnyPattern(emptySet()))
        assertTrue(jararacucu.copy(colorPatterns = emptySet()).hasAnyPattern(emptySet()))
    }

    @Test
    fun `matches when the species has any of the selected patterns`() {
        assertTrue(jararacucu.hasAnyPattern(setOf(ColorPattern.VARIEGATED)))
        assertTrue(jararacucu.hasAnyPattern(setOf(ColorPattern.GREEN, ColorPattern.VARIEGATED)))
        assertFalse(jararacucu.hasAnyPattern(setOf(ColorPattern.GREEN, ColorPattern.LINED)))
    }

    @Test
    fun `species without patterns only show with no filter`() {
        assertFalse(jararacucu.copy(colorPatterns = emptySet()).hasAnyPattern(setOf(ColorPattern.UNIFORM)))
    }
}
