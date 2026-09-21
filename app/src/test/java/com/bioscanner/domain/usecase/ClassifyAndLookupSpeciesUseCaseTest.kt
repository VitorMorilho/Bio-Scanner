package com.bioscanner.domain.usecase

import android.graphics.Bitmap
import com.bioscanner.domain.classifier.ImageClassifier
import com.bioscanner.domain.model.ClassificationCandidate
import com.bioscanner.domain.model.ClassificationResult
import com.bioscanner.domain.model.RiskLevel
import com.bioscanner.domain.model.Species
import com.bioscanner.domain.repository.SpeciesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

private val FAKE_SPECIES = Species(
    id = "sp1",
    scientificName = "Species one",
    popularName = "Popular one",
    family = "Familia",
    category = "snake",
    venomous = true,
    riskLevel = RiskLevel.HIGH,
    description = "desc",
    geographicDistribution = "dist",
    firstAidProcedure = "procedure",
    referenceImagePath = null,
)

private class FakeImageClassifier(private val result: List<ClassificationCandidate>) : ImageClassifier {
    override suspend fun classify(bitmap: Bitmap): List<ClassificationCandidate> = result
}

private class FakeSpeciesRepository(private val bylabel: Map<String, Species>) : SpeciesRepository {
    override fun observeAllSpecies(): Flow<List<Species>> = flowOf(bylabel.values.toList())
    override fun observeSpeciesById(id: String): Flow<Species?> = flowOf(bylabel.values.find { it.id == id })
    override fun observeSpeciesByCategory(category: String): Flow<List<Species>> = flowOf(emptyList())
    override suspend fun findByClassifierLabel(label: String): Species? = bylabel[label]
}

class ClassifyAndLookupSpeciesUseCaseTest {

    private val bitmap = mock(Bitmap::class.java)

    @Test
    fun `confident top candidate found in repository returns Success`() = runBlocking {
        val useCase = ClassifyAndLookupSpeciesUseCase(
            classifier = FakeImageClassifier(listOf(ClassificationCandidate("sp1", 0.9f))),
            repository = FakeSpeciesRepository(mapOf("sp1" to FAKE_SPECIES)),
        )

        val result = useCase(bitmap)

        assertTrue(result is ClassificationResult.Success)
        result as ClassificationResult.Success
        assertEquals(FAKE_SPECIES, result.species)
        assertEquals(0.9f, result.confidence)
    }

    @Test
    fun `best candidate below threshold returns LowConfidence`() = runBlocking {
        val candidates = listOf(ClassificationCandidate("sp1", 0.4f))
        val useCase = ClassifyAndLookupSpeciesUseCase(
            classifier = FakeImageClassifier(candidates),
            repository = FakeSpeciesRepository(mapOf("sp1" to FAKE_SPECIES)),
        )

        val result = useCase(bitmap)

        assertTrue(result is ClassificationResult.LowConfidence)
        assertEquals(candidates, (result as ClassificationResult.LowConfidence).candidates)
    }

    @Test
    fun `empty candidate list returns NoMatch`() = runBlocking {
        val useCase = ClassifyAndLookupSpeciesUseCase(
            classifier = FakeImageClassifier(emptyList()),
            repository = FakeSpeciesRepository(emptyMap()),
        )

        val result = useCase(bitmap)

        assertTrue(result is ClassificationResult.NoMatch)
    }

    @Test
    fun `confident candidate whose label is missing from repository returns NoMatch`() = runBlocking {
        val useCase = ClassifyAndLookupSpeciesUseCase(
            classifier = FakeImageClassifier(listOf(ClassificationCandidate("unknown_label", 0.9f))),
            repository = FakeSpeciesRepository(mapOf("sp1" to FAKE_SPECIES)),
        )

        val result = useCase(bitmap)

        assertTrue(result is ClassificationResult.NoMatch)
    }
}
