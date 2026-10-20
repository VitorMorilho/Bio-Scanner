package com.bioscanner.domain.usecase

import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.model.GeoPoint
import com.bioscanner.domain.model.MetadataOrigin
import com.bioscanner.domain.model.Observation
import com.bioscanner.domain.observation.ObservationSyncer
import com.bioscanner.domain.observation.PhotoStorage
import com.bioscanner.domain.repository.ObservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.Test

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
        override fun observePendingUploadCount(): Flow<Int> = emptyFlow()
        override suspend fun create(photoPath: String, metadata: CaptureMetadata): Long {
            created += photoPath to metadata
            return 42
        }
        override suspend fun findById(id: Long): Observation? = null
        override suspend fun delete(id: Long) = Unit
        override suspend fun deleteAll() = Unit
    }

    private var syncRequests = 0

    private val syncer = object : ObservationSyncer {
        override fun requestSync() {
            syncRequests++
        }
        override suspend fun syncNow(userId: String) = Unit
        override suspend fun clearLocal(userId: String) = Unit
    }

    @Test
    fun `saves a copy of the photo and stores it with its metadata`() = runTest {
        val repository = FakeRepository()
        val useCase = RegisterObservationUseCase(
            photoStorage = object : PhotoStorage {
                override suspend fun import(photoUri: String) = "/files/observations/copy.jpg"
                override suspend fun delete(photoPath: String) = Unit
                override suspend fun deleteAll() = Unit
                override suspend fun readBytes(photoPath: String): ByteArray? = null
                override suspend fun save(bytes: ByteArray) = ""
            },
            repository = repository,
            syncer = syncer,
        )

        val id = useCase("content://media/picker/1", metadata)

        assertEquals(42L, id)
        assertEquals(listOf("/files/observations/copy.jpg" to metadata), repository.created)
        assertEquals(1, syncRequests)
    }
}
