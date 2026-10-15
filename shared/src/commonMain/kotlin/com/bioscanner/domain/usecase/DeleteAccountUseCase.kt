package com.bioscanner.domain.usecase

import com.bioscanner.domain.auth.AuthRepository
import com.bioscanner.domain.observation.PhotoStorage
import com.bioscanner.domain.repository.ObservationRepository

/**
 * Exclusão completa da conta: servidor + tudo que o app guardou no aparelho.
 *
 * Servidor primeiro: se a chamada falhar (sem internet, por exemplo), nada local é apagado e o
 * usuário pode tentar de novo. Já apagar os registros antes e falhar no servidor deixaria a
 * conta viva e os dados perdidos.
 */
class DeleteAccountUseCase(
    private val authRepository: AuthRepository,
    private val observationRepository: ObservationRepository,
    private val photoStorage: PhotoStorage,
) {
    suspend operator fun invoke() {
        authRepository.deleteAccount()
        // Mesma ordem do DeleteObservationUseCase: banco antes dos arquivos.
        observationRepository.deleteAll()
        photoStorage.deleteAll()
    }
}
