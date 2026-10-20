package com.bioscanner.data.sync

import com.bioscanner.data.local.db.ObservationDao
import com.bioscanner.data.local.db.ObservationEntity
import com.bioscanner.data.local.db.SyncState
import com.bioscanner.domain.observation.PhotoStorage
import com.bioscanner.presentation.platform.logWarning
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "ObservationSync"

// Mesmo valor que ObservationRepositoryImpl grava (coluna herdada, ver ObservationEntity).
private const val STATUS_SAVED = "SAVED"

/**
 * Deixa os registros de um usuário iguais no aparelho e na nuvem. A nuvem é a referência: o que
 * foi apagado lá (por outro aparelho) sai daqui, o que existe lá e não aqui é baixado.
 *
 * Uma sincronização por vez (mutex): o envio de um registro não pode correr junto com a limpeza
 * do logout, nem duas rodadas baixarem o mesmo registro.
 */
@OptIn(ExperimentalUuidApi::class)
class ObservationSync(
    private val dao: ObservationDao,
    private val remote: ObservationRemote,
    private val photoStorage: PhotoStorage,
    private val newRemoteId: () -> String = { Uuid.random().toString() },
) {
    private val mutex = Mutex()

    suspend fun sync(userId: String) = mutex.withLock {
        // Registros feitos sem conta passam a ser desta conta (decisão de produto: entrar "adota").
        dao.adoptOrphans(userId)
        // Registros de antes da v5 do banco ainda não têm id na nuvem.
        dao.findWithoutRemoteId().forEach { dao.setRemoteId(it.id, newRemoteId()) }

        pushDeletes(userId)
        pushUploads(userId)
        pull(userId)
    }

    suspend fun clearLocal(userId: String) = mutex.withLock {
        val photos = dao.findByUser(userId).map { it.photoPath }
        // Banco antes dos arquivos, como no DeleteObservationUseCase.
        dao.deleteByUser(userId)
        photos.forEach { photoStorage.delete(it) }
    }

    private suspend fun pushDeletes(userId: String) {
        dao.findByState(userId, SyncState.PENDING_DELETE).forEach { entity ->
            entity.remoteId?.let { remote.delete(userId, it) }
            dao.deleteById(entity.id)
        }
    }

    private suspend fun pushUploads(userId: String) {
        dao.findByState(userId, SyncState.PENDING_UPLOAD).forEach { entity ->
            val remoteId = entity.remoteId ?: return@forEach
            val photo = photoStorage.readBytes(entity.photoPath)
            if (photo == null) {
                // Sem a foto o registro não tem como subir; fica pendente em vez de travar os outros.
                logWarning(TAG, "Foto do registro ${entity.id} não encontrada; envio adiado")
                return@forEach
            }
            remote.upload(userId, entity.toRemote(remoteId), photo)
            dao.setSyncState(entity.id, SyncState.SYNCED)
        }
    }

    private suspend fun pull(userId: String) {
        val remoteObservations = remote.list(userId)
        val remoteIds = remoteObservations.map { it.id }.toSet()
        val local = dao.findByUser(userId)

        // Apagados em outro aparelho. Só os já enviados: um pendente ainda não chegou lá.
        local.filter { it.syncState == SyncState.SYNCED && it.remoteId !in remoteIds }.forEach {
            dao.deleteById(it.id)
            photoStorage.delete(it.photoPath)
        }

        val localRemoteIds = local.mapNotNull { it.remoteId }.toSet()
        remoteObservations.filter { it.id !in localRemoteIds }.forEach { observation ->
            val photoPath = photoStorage.save(remote.downloadPhoto(userId, observation.id))
            dao.insert(observation.toEntity(userId, photoPath))
        }
    }
}

private fun ObservationEntity.toRemote(remoteId: String) = RemoteObservation(
    id = remoteId,
    takenAt = takenAt,
    takenAtOrigin = takenAtOrigin,
    latitude = latitude,
    longitude = longitude,
    locationOrigin = locationOrigin,
    createdAt = createdAt,
)

private fun RemoteObservation.toEntity(userId: String, photoPath: String) = ObservationEntity(
    photoPath = photoPath,
    takenAt = takenAt,
    takenAtOrigin = takenAtOrigin,
    latitude = latitude,
    longitude = longitude,
    locationOrigin = locationOrigin,
    status = STATUS_SAVED,
    speciesId = null,
    confidence = null,
    createdAt = createdAt,
    remoteId = id,
    userId = userId,
    syncState = SyncState.SYNCED,
)
