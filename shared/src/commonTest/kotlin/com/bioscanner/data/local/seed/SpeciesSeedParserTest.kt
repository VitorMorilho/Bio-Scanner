package com.bioscanner.data.local.seed

import com.bioscanner.data.local.db.SpeciesRegionEntity
import com.bioscanner.data.repository.toDomain
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

class SpeciesSeedParserTest {

    private val raw = """
        {"version":"abc123","species":[
          {"id":"crotalus_durissus","gbifKey":2444433,"scientificName":"Crotalus durissus",
           "popularName":"Cascavel","otherNames":["Maracá","Boicininga"],"family":"Viperidae",
           "venomous":true,"riskLevel":"HIGH","dentition":"solenóglifa","diet":"Roedores",
           "activityPeriod":"Noturno","defensiveBehavior":"Agita o chocalho",
           "regions":[{"regionId":"MT","occurrences":46},{"regionId":"GO","occurrences":12}]},
          {"id":"pseudoboa_nigra","scientificName":"Pseudoboa nigra","family":"Colubridae",
           "venomous":false}
        ]}
    """.trimIndent()

    @Test
    fun `parses version species and flattens regions into join rows`() {
        val seed = parseSeed(raw)

        assertEquals("abc123", seed.version)
        assertEquals(listOf("crotalus_durissus", "pseudoboa_nigra"), seed.species.map { it.id })
        assertEquals(
            listOf(
                SpeciesRegionEntity("crotalus_durissus", "MT", 46),
                SpeciesRegionEntity("crotalus_durissus", "GO", 12),
            ),
            seed.regions,
        )
    }

    @Test
    fun `curated fields round-trip to the domain model`() {
        val cascavel = parseSeed(raw).species.first().toDomain()

        assertEquals("Cascavel", cascavel.displayName)
        assertEquals(listOf("Maracá", "Boicininga"), cascavel.otherNames)
        assertEquals("solenóglifa", cascavel.dentition)
        assertEquals("Roedores", cascavel.diet)
        assertEquals("Noturno", cascavel.activityPeriod)
        assertEquals("Agita o chocalho", cascavel.defensiveBehavior)
    }

    @Test
    fun `species without curated data keep nulls and fall back to scientific name`() {
        val entity = parseSeed(raw).species.last()
        val species = entity.toDomain()

        assertEquals("pseudoboa_nigra", entity.classifierLabel)
        assertEquals("Pseudoboa nigra", species.displayName)
        assertEquals(emptyList<String>(), species.otherNames)
        assertNull(species.defensiveBehavior)
        assertNull(species.firstAidProcedure)
    }
}
