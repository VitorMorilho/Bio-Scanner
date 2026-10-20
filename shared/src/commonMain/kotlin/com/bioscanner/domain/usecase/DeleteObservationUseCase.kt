package com.bioscanner.domain.usecase

import com.bioscanner.domain.observation.ObservationSyncer
import com.bioscanner.domain.observation.PhotoStorage
import com.bioscanner.domain.repository.ObservationRepository

/**
 * Exclui o registro e a cópia da foto salva pelo app (a foto original na galeria não é tocada).
 * Com login, o registro também sai da nuvem na sincronização pedida aqui.
 */
class DeleteObservationUseCase(
    private val repository: ObservationRepository,
    private val photoStorage: PhotoStorage,
    private val syncer: ObservationSyncer,
) {
    suspend operator fun invoke(id: Long) {
        val observation = repository.findById(id) ?: return
        // Banco primeiro: se apagar o arquivo falhar, sobra só um arquivo órfão,
        // nunca um registro apontando para uma foto que não existe.
        repository.delete(id)
        photoStorage.delete(observation.photoPath)
        syncer.requestSync()
    }
}
