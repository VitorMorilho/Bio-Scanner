package com.bioscanner.data.repository

import com.bioscanner.data.local.db.ObservationDao
import com.bioscanner.data.local.db.ObservationEntity
import com.bioscanner.data.local.db.SyncState
import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.model.GeoPoint
import com.bioscanner.domain.model.MetadataOrigin
import com.bioscanner.domain.model.Observation
import com.bioscanner.domain.repository.ObservationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Valor fixo da coluna status, herdada da versão com análise (ver ObservationEntity).
private const val STATUS_SAVED = "SAVED"

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class ObservationRepositoryImpl(
    private val dao: ObservationDao,
    /** Id da conta logada (null = sem login). */
    private val currentUserId: Flow<String?>,
    /** Relógio em epoch millis; parâmetro porque não há System.currentTimeMillis no código comum. */
    private val now: () -> Long,
    private val newRemoteId: () -> String = { Uuid.random().toString() },
) : ObservationRepository {

    override fun observeAll(): Flow<List<Observation>> =
        currentUserId.flatMapLatest { userId -> dao.getAllFor(userId) }.map { list -> list.map { it.toDomain() } }

    override fun observePendingUploadCount(): Flow<Int> = currentUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(0) else dao.countByState(userId, SyncState.PENDING_UPLOAD)
    }

    override fun observeById(id: Long): Flow<Observation?> =
        dao.getById(id).map { it?.toDomain() }

    override suspend fun create(photoPath: String, metadata: CaptureMetadata): Long {
        val userId = currentUserId.first()
        return dao.insert(
            ObservationEntity(
                photoPath = photoPath,
                takenAt = metadata.takenAt,
                takenAtOrigin = metadata.takenAtOrigin.name,
                latitude = metadata.location?.latitude,
                longitude = metadata.location?.longitude,
                locationOrigin = metadata.locationOrigin?.name,
                status = STATUS_SAVED,
                speciesId = null,
                confidence = null,
                createdAt = now(),
                remoteId = newRemoteId(),
                userId = userId,
                syncState = if (userId == null) SyncState.LOCAL else SyncState.PENDING_UPLOAD,
            ),
        )
    }

    override suspend fun findById(id: Long): Observation? = dao.findById(id)?.toDomain()

    override suspend fun delete(id: Long) {
        val entity = dao.findById(id) ?: return
        if (entity.syncState == SyncState.LOCAL) {
            dao.deleteById(id)
        } else {
            // Pode já estar na nuvem (ou com o envio no meio): a sincronização apaga lá e depois aqui.
            dao.setSyncState(id, SyncState.PENDING_DELETE)
        }
    }

    override suspend fun deleteAll() = dao.deleteAll()
}

fun ObservationEntity.toDomain(): Observation {
    val location = if (latitude != null && longitude != null) GeoPoint(latitude, longitude) else null
    return Observation(
        id = id,
        photoPath = photoPath,
        metadata = CaptureMetadata(
            takenAt = takenAt,
            takenAtOrigin = enumValueOrNull<MetadataOrigin>(takenAtOrigin) ?: MetadataOrigin.DEVICE,
            location = location,
            locationOrigin = locationOrigin?.let { enumValueOrNull<MetadataOrigin>(it) },
        ),
        createdAt = createdAt,
    )
}

private inline fun <reified T : Enum<T>> enumValueOrNull(name: String): T? =
    enumValues<T>().find { it.name == name }
