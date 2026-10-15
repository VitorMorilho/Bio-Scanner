package com.bioscanner.domain.usecase

import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.observation.PhotoStorage
import com.bioscanner.domain.repository.ObservationRepository

/** Salva o registro (cópia da foto + data/hora + local) nos registros do usuário. Retorna o id. */
class RegisterObservationUseCase(
    private val photoStorage: PhotoStorage,
    private val repository: ObservationRepository,
) {
    suspend operator fun invoke(photoUri: String, metadata: CaptureMetadata): Long {
        val photoPath = photoStorage.import(photoUri)
        return repository.create(photoPath, metadata)
    }
}
