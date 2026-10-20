package com.bioscanner.domain.usecase

import com.bioscanner.domain.auth.AuthRepository
import com.bioscanner.domain.observation.ObservationSyncer
import com.bioscanner.domain.repository.ObservationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

sealed interface SignOutResult {
    data object SignedOut : SignOutResult

    /** Nada foi feito: há registros que ainda não subiram e se perderiam ao sair. */
    data class HasPendingUploads(val count: Int) : SignOutResult
}

/**
 * Sai da conta e tira do aparelho os registros dela (eles continuam na nuvem e voltam no próximo
 * login). Antes tenta enviar o que estiver pendente; se ainda sobrar algo, só sai com [force].
 */
class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val repository: ObservationRepository,
    private val syncer: ObservationSyncer,
) {
    suspend operator fun invoke(force: Boolean = false): SignOutResult {
        val userId = authRepository.currentUser.first()?.id
        if (userId != null && !force) {
            try {
                syncer.syncNow(userId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sem internet: o que não subiu é contado abaixo e o usuário decide.
            }
            val pending = repository.observePendingUploadCount().first()
            if (pending > 0) return SignOutResult.HasPendingUploads(pending)
        }
        // Servidor antes: se o logout falhar, os registros continuam no aparelho.
        authRepository.signOut()
        userId?.let { syncer.clearLocal(it) }
        return SignOutResult.SignedOut
    }
}
