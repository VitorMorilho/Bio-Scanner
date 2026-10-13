package com.bioscanner.data.repository

import com.bioscanner.data.local.db.ObservationDao
import com.bioscanner.data.local.db.ObservationEntity
import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.model.GeoPoint
import com.bioscanner.domain.model.MetadataOrigin
import com.bioscanner.domain.model.Observation
import com.bioscanner.domain.repository.ObservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Valor fixo da coluna status, herdada da versão com análise (ver ObservationEntity).
private const val STATUS_SAVED = "SAVED"

class ObservationRepositoryImpl @Inject constructor(
    private val dao: ObservationDao,
) : ObservationRepository {

    override fun observeAll(): Flow<List<Observation>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Observation?> =
        dao.getById(id).map { it?.toDomain() }

    override suspend fun create(photoPath: String, metadata: CaptureMetadata): Long = dao.insert(
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
            createdAt = System.currentTimeMillis(),
        ),
    )

    override suspend fun findById(id: Long): Observation? = dao.findById(id)?.toDomain()

    override suspend fun delete(id: Long) = dao.deleteById(id)
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
