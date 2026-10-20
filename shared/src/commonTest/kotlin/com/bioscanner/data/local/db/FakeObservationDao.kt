package com.bioscanner.data.local.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** ObservationDao em memória, com a mesma semântica das queries do Room. */
class FakeObservationDao(initial: List<ObservationEntity> = emptyList()) : ObservationDao {

    val rows = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    fun row(id: Long): ObservationEntity? = rows.value.find { it.id == id }

    override fun getAllFor(userId: String?): Flow<List<ObservationEntity>> = rows.map { list ->
        list.filter { it.userId == userId && it.syncState != SyncState.PENDING_DELETE }.sortedByDescending { it.takenAt }
    }

    override fun getById(id: Long): Flow<ObservationEntity?> = rows.map { list -> list.find { it.id == id } }

    override suspend fun findById(id: Long): ObservationEntity? = row(id)

    override suspend fun insert(observation: ObservationEntity): Long {
        val remoteId = observation.remoteId
        check(remoteId == null || rows.value.none { it.remoteId == remoteId }) { "UNIQUE remoteId" }
        val id = nextId++
        rows.update { it + observation.copy(id = id) }
        return id
    }

    override suspend fun deleteById(id: Long) = rows.update { list -> list.filterNot { it.id == id } }

    override suspend fun deleteAll() = rows.update { emptyList() }

    override suspend fun adoptOrphans(userId: String) = rows.update { list ->
        list.map { if (it.userId == null) it.copy(userId = userId, syncState = SyncState.PENDING_UPLOAD) else it }
    }

    override suspend fun findWithoutRemoteId(): List<ObservationEntity> = rows.value.filter { it.remoteId == null }

    override suspend fun setRemoteId(id: Long, remoteId: String) = edit(id) { it.copy(remoteId = remoteId) }

    override suspend fun findByState(userId: String, syncState: String): List<ObservationEntity> =
        rows.value.filter { it.userId == userId && it.syncState == syncState }

    override suspend fun setSyncState(id: Long, syncState: String) = edit(id) { it.copy(syncState = syncState) }

    override fun countByState(userId: String, syncState: String): Flow<Int> =
        rows.map { list -> list.count { it.userId == userId && it.syncState == syncState } }

    override suspend fun findByUser(userId: String): List<ObservationEntity> = rows.value.filter { it.userId == userId }

    override suspend fun deleteByUser(userId: String) = rows.update { list -> list.filterNot { it.userId == userId } }

    private fun edit(id: Long, change: (ObservationEntity) -> ObservationEntity) =
        rows.update { list -> list.map { if (it.id == id) change(it) else it } }
}

fun observationEntity(
    id: Long,
    userId: String? = null,
    syncState: String = SyncState.LOCAL,
    remoteId: String? = "remote-$id",
    photoPath: String = "/photos/$id.jpg",
) = ObservationEntity(
    id = id,
    photoPath = photoPath,
    takenAt = 1_700_000_000_000 + id,
    takenAtOrigin = "DEVICE",
    latitude = -15.6,
    longitude = -56.1,
    locationOrigin = "DEVICE",
    status = "SAVED",
    speciesId = null,
    confidence = null,
    createdAt = 1_700_000_000_000,
    remoteId = remoteId,
    userId = userId,
    syncState = syncState,
)
