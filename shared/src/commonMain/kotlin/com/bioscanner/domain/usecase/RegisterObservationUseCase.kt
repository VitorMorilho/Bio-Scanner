package com.bioscanner.domain.usecase

import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.observation.ObservationSyncer
import com.bioscanner.domain.observation.PhotoStorage
import com.bioscanner.domain.repository.ObservationRepository

/**
 * Salva o registro (cópia da foto + data/hora + local) nos registros do usuário. Retorna o id.
 * Com login, o envio para a nuvem acontece em seguida, sem fazer a tela esperar.
 */
class RegisterObservationUseCase(
    private val photoStorage: PhotoStorage,
    private val repository: ObservationRepository,
    private val syncer: ObservationSyncer,
) {
    suspend operator fun invoke(photoUri: String, metadata: CaptureMetadata): Long {
        val photoPath = photoStorage.import(photoUri)
        val id = repository.create(photoPath, metadata)
        syncer.requestSync()
        return id
    }
}
