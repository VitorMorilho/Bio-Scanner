package com.bioscanner.data.classifier

import android.graphics.Bitmap
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
import org.mockito.Mockito.`when`

private val SAMPLE_SPECIES = listOf(
    Species("sp1", "Sci one", "Pop one", "Fam", "snake", true, RiskLevel.HIGH, "d", "g", "f", null),
    Species("sp2", "Sci two", "Pop two", "Fam", "snake", false, RiskLevel.LOW, "d", "g", "f", null),
    Species("sp3", "Sci three", "Pop three", "Fam", "snake", true, RiskLevel.MEDIUM, "d", "g", "f", null),
)

private class FakeRepository(private val species: List<Species>) : SpeciesRepository {
    override fun observeAllSpecies(): Flow<List<Species>> = flowOf(species)
    override fun observeSpeciesById(id: String): Flow<Species?> = flowOf(species.find { it.id == id })
    override fun observeSpeciesByCategory(category: String): Flow<List<Species>> = flowOf(species)
    override suspend fun findByClassifierLabel(label: String): Species? = species.find { it.id == label }
}

class MockImageClassifierTest {

    private fun bitmapOf(width: Int, height: Int, byteCount: Int): Bitmap {
        val bitmap = mock(Bitmap::class.java)
        `when`(bitmap.width).thenReturn(width)
        `when`(bitmap.height).thenReturn(height)
        `when`(bitmap.byteCount).thenReturn(byteCount)
        return bitmap
    }

    @Test
    fun `same bitmap properties produce the same result every time`() = runBlocking {
        val classifier = MockImageClassifier(FakeRepository(SAMPLE_SPECIES))
        val bitmap = bitmapOf(100, 200, 80000)

        val first = classifier.classify(bitmap)
        val second = classifier.classify(bitmap)

        assertEquals(first, second)
    }

    @Test
    fun `confidences are within 0 and 1 and sorted descending`() = runBlocking {
        val classifier = MockImageClassifier(FakeRepository(SAMPLE_SPECIES))
        val bitmap = bitmapOf(50, 50, 10000)

        val candidates = classifier.classify(bitmap)

        assertTrue(candidates.isNotEmpty())
        candidates.forEach { assertTrue(it.confidence in 0f..1f) }
        assertEquals(candidates.sortedByDescending { it.confidence }, candidates)
    }

    @Test
    fun `empty species list yields empty candidate list`() = runBlocking {
        val classifier = MockImageClassifier(FakeRepository(emptyList()))
        val bitmap = bitmapOf(50, 50, 10000)

        assertTrue(classifier.classify(bitmap).isEmpty())
    }
}
