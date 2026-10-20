package com.bioscanner.data.repository

import com.bioscanner.data.local.db.FakeObservationDao
import com.bioscanner.data.local.db.SyncState
import com.bioscanner.data.local.db.observationEntity
import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.model.MetadataOrigin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObservationRepositoryImplTest {

    private val metadata = CaptureMetadata(0, MetadataOrigin.DEVICE, null, null)
    private val userId = MutableStateFlow<String?>(null)

    private fun repository(dao: FakeObservationDao) =
        ObservationRepositoryImpl(dao, currentUserId = userId, now = { 7 }, newRemoteId = { "uuid" })

    @Test
    fun `record made without an account stays local`() = runTest {
        val dao = FakeObservationDao()

        val id = repository(dao).create("/p.jpg", metadata)

        val row = dao.row(id)!!
        assertNull(row.userId)
        assertEquals(SyncState.LOCAL, row.syncState)
        assertEquals("uuid", row.remoteId)
    }

    @Test
    fun `record made while signed in belongs to the account and waits for upload`() = runTest {
        userId.value = "user-1"
        val dao = FakeObservationDao()

        val id = repository(dao).create("/p.jpg", metadata)

        assertEquals("user-1", dao.row(id)!!.userId)
        assertEquals(SyncState.PENDING_UPLOAD, dao.row(id)!!.syncState)
        assertEquals(1, repository(dao).observePendingUploadCount().first())
    }

    @Test
    fun `lists only the records of whoever is using the app`() = runTest {
        val dao = FakeObservationDao(
            listOf(
                observationEntity(1, userId = null),
                observationEntity(2, "user-1", SyncState.SYNCED),
                observationEntity(3, "user-1", SyncState.PENDING_DELETE),
                observationEntity(4, "user-2", SyncState.SYNCED),
            ),
        )
        val repository = repository(dao)

        assertEquals(listOf(1L), repository.observeAll().first().map { it.id })
        userId.value = "user-1"
        assertEquals(listOf(2L), repository.observeAll().first().map { it.id })
    }

    @Test
    fun `deleting a local record removes it and an account record waits for the cloud`() = runTest {
        val dao = FakeObservationDao(
            listOf(
                observationEntity(1, userId = null),
                observationEntity(2, "user-1", SyncState.SYNCED),
            ),
        )
        val repository = repository(dao)

        repository.delete(1)
        repository.delete(2)

        assertNull(dao.row(1))
        assertEquals(SyncState.PENDING_DELETE, dao.row(2)!!.syncState)
    }
}
