package com.bioscanner.domain.usecase

import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.model.GeoPoint
import com.bioscanner.domain.model.MetadataOrigin
import com.bioscanner.domain.model.Observation
import com.bioscanner.domain.observation.PhotoStorage
import com.bioscanner.domain.repository.ObservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RegisterObservationUseCaseTest {

    private val metadata = CaptureMetadata(
        takenAt = 1_700_000_000_000,
        takenAtOrigin = MetadataOrigin.PHOTO,
        location = GeoPoint(-15.6, -56.1),
        locationOrigin = MetadataOrigin.DEVICE,
    )

    private class FakeRepository : ObservationRepository {
        val created = mutableListOf<Pair<String, CaptureMetadata>>()
        override fun observeAll(): Flow<List<Observation>> = emptyFlow()
        override fun observeById(id: Long): Flow<Observation?> = emptyFlow()
        override suspend fun create(photoPath: String, metadata: CaptureMetadata): Long {
            created += photoPath to metadata
            return 42
        }
        override suspend fun findById(id: Long): Observation? = null
        override suspend fun delete(id: Long) = Unit
    }

    @Test
    fun `saves a copy of the photo and stores it with its metadata`() = runBlocking {
        val repository = FakeRepository()
        val useCase = RegisterObservationUseCase(
            photoStorage = object : PhotoStorage {
                override suspend fun import(photoUri: String) = "/files/observations/copy.jpg"
                override suspend fun delete(photoPath: String) = Unit
            },
            repository = repository,
        )

        val id = useCase("content://media/picker/1", metadata)

        assertEquals(42L, id)
        assertEquals(listOf("/files/observations/copy.jpg" to metadata), repository.created)
    }
}
