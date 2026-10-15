package com.bioscanner.domain.usecase

import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.model.MetadataOrigin
import com.bioscanner.domain.model.Observation
import com.bioscanner.domain.observation.PhotoStorage
import com.bioscanner.domain.repository.ObservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.Test

class DeleteObservationUseCaseTest {

    private val events = mutableListOf<String>()

    private val repository = object : ObservationRepository {
        val stored = mutableMapOf(
            1L to Observation(
                id = 1,
                photoPath = "/files/observations/a.jpg",
                metadata = CaptureMetadata(0, MetadataOrigin.DEVICE, null, null),
                createdAt = 0,
            ),
        )
        override fun observeAll(): Flow<List<Observation>> = emptyFlow()
        override fun observeById(id: Long): Flow<Observation?> = emptyFlow()
        override suspend fun create(photoPath: String, metadata: CaptureMetadata): Long = 0
        override suspend fun findById(id: Long): Observation? = stored[id]
        override suspend fun delete(id: Long) {
            stored.remove(id)
            events += "db:$id"
        }
        override suspend fun deleteAll() = Unit
    }

    private val photoStorage = object : PhotoStorage {
        override suspend fun import(photoUri: String) = ""
        override suspend fun delete(photoPath: String) {
            events += "file:$photoPath"
        }
        override suspend fun deleteAll() = Unit
    }

    private val useCase = DeleteObservationUseCase(repository, photoStorage)

    @Test
    fun `deletes the row first and then its photo`() = runTest {
        useCase(1)

        assertEquals(listOf("db:1", "file:/files/observations/a.jpg"), events)
        assertEquals(emptyMap<Long, Observation>(), repository.stored)
    }

    @Test
    fun `unknown id does nothing`() = runTest {
        useCase(99)

        assertEquals(emptyList<String>(), events)
    }
}
