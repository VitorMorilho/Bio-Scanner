package com.bioscanner.data.sync

import com.bioscanner.data.local.db.FakeObservationDao
import com.bioscanner.data.local.db.SyncState
import com.bioscanner.data.local.db.observationEntity
import com.bioscanner.domain.observation.PhotoStorage
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val USER = "user-1"

class ObservationSyncTest {

    private class FakeRemote : ObservationRemote {
        val rows = mutableMapOf<String, RemoteObservation>()
        val photos = mutableMapOf<String, ByteArray>()
        var failUploads = false

        override suspend fun list(userId: String) = rows.values.toList()

        override suspend fun upload(userId: String, observation: RemoteObservation, photo: ByteArray) {
            if (failUploads) throw IOException("sem internet")
            photos[observation.id] = photo
            rows[observation.id] = observation
        }

        override suspend fun downloadPhoto(userId: String, id: String) = photos.getValue(id)

        override suspend fun delete(userId: String, id: String) {
            rows.remove(id)
            photos.remove(id)
        }
    }

    private class FakePhotos : PhotoStorage {
        val files = mutableMapOf<String, ByteArray>()
        private var saved = 0

        override suspend fun import(photoUri: String) = error("não usado")
        override suspend fun delete(photoPath: String) {
            files.remove(photoPath)
        }
        override suspend fun deleteAll() = files.clear()
        override suspend fun readBytes(photoPath: String) = files[photoPath]
        override suspend fun save(bytes: ByteArray): String {
            val path = "/downloaded/${++saved}.jpg"
            files[path] = bytes
            return path
        }
    }

    private val remote = FakeRemote()
    private val photos = FakePhotos()
    private var ids = 0

    private fun sync(dao: FakeObservationDao) = ObservationSync(dao, remote, photos, newRemoteId = { "new-${++ids}" })

    private fun remoteObservation(id: String) = RemoteObservation(
        id = id, takenAt = 5, takenAtOrigin = "PHOTO", latitude = null, longitude = null, locationOrigin = null, createdAt = 6,
    )

    @Test
    fun `adopts records made without an account and uploads them with their photo`() = runTest {
        val dao = FakeObservationDao(listOf(observationEntity(1, userId = null, remoteId = null)))
        photos.files["/photos/1.jpg"] = byteArrayOf(1, 2, 3)

        sync(dao).sync(USER)

        val row = dao.row(1)!!
        assertEquals(USER, row.userId)
        assertEquals(SyncState.SYNCED, row.syncState)
        assertEquals("new-1", row.remoteId)
        assertEquals(setOf("new-1"), remote.rows.keys)
        assertContentEquals(byteArrayOf(1, 2, 3), remote.photos["new-1"])
    }

    @Test
    fun `failed upload keeps the record pending`() = runTest {
        val dao = FakeObservationDao(listOf(observationEntity(1, USER, SyncState.PENDING_UPLOAD)))
        photos.files["/photos/1.jpg"] = byteArrayOf(1)
        remote.failUploads = true

        assertFailsWith<IOException> { sync(dao).sync(USER) }

        assertEquals(SyncState.PENDING_UPLOAD, dao.row(1)!!.syncState)
    }

    @Test
    fun `record whose photo is missing stays pending without blocking the others`() = runTest {
        val dao = FakeObservationDao(
            listOf(
                observationEntity(1, USER, SyncState.PENDING_UPLOAD),
                observationEntity(2, USER, SyncState.PENDING_UPLOAD),
            ),
        )
        photos.files["/photos/2.jpg"] = byteArrayOf(2)

        sync(dao).sync(USER)

        assertEquals(SyncState.PENDING_UPLOAD, dao.row(1)!!.syncState)
        assertEquals(SyncState.SYNCED, dao.row(2)!!.syncState)
    }

    @Test
    fun `pending delete leaves the cloud and then the device`() = runTest {
        remote.rows["remote-1"] = remoteObservation("remote-1")
        val dao = FakeObservationDao(listOf(observationEntity(1, USER, SyncState.PENDING_DELETE)))

        sync(dao).sync(USER)

        assertTrue(remote.rows.isEmpty())
        assertNull(dao.row(1))
    }

    @Test
    fun `downloads records that exist only in the cloud`() = runTest {
        remote.rows["from-other-phone"] = remoteObservation("from-other-phone")
        remote.photos["from-other-phone"] = byteArrayOf(9)
        val dao = FakeObservationDao()

        sync(dao).sync(USER)

        val row = dao.rows.value.single()
        assertEquals("from-other-phone", row.remoteId)
        assertEquals(USER, row.userId)
        assertEquals(SyncState.SYNCED, row.syncState)
        assertEquals(5, row.takenAt)
        assertContentEquals(byteArrayOf(9), photos.files[row.photoPath])
    }

    @Test
    fun `synced record deleted on another phone is removed here`() = runTest {
        val dao = FakeObservationDao(listOf(observationEntity(1, USER, SyncState.SYNCED)))
        photos.files["/photos/1.jpg"] = byteArrayOf(1)

        sync(dao).sync(USER)

        assertNull(dao.row(1))
        assertNull(photos.files["/photos/1.jpg"])
    }

    @Test
    fun `clearLocal removes only the records and photos of that user`() = runTest {
        val dao = FakeObservationDao(
            listOf(
                observationEntity(1, USER, SyncState.SYNCED),
                observationEntity(2, userId = null),
            ),
        )
        photos.files["/photos/1.jpg"] = byteArrayOf(1)
        photos.files["/photos/2.jpg"] = byteArrayOf(2)

        sync(dao).clearLocal(USER)

        assertEquals(listOf(2L), dao.rows.value.map { it.id })
        assertEquals(setOf("/photos/2.jpg"), photos.files.keys)
    }
}
