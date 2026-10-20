package com.bioscanner.data.sync

import com.bioscanner.domain.auth.AuthRepository
import com.bioscanner.domain.observation.ObservationSyncer
import com.bioscanner.presentation.platform.logWarning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "ObservationSync"

/**
 * Quando sincronizar: ao abrir o app, ao entrar na conta e a cada pedido (registro criado ou
 * apagado). Pedidos feitos durante uma rodada viram uma só rodada depois dela (canal CONFLATED).
 *
 * Sem envio em segundo plano com o app fechado: o que falhar (ex.: sem internet em campo) fica
 * pendente e vai na próxima abertura ou no próximo registro.
 */
class ObservationSyncCoordinator(
    authRepository: AuthRepository,
    private val sync: ObservationSync,
    scope: CoroutineScope,
) : ObservationSyncer {

    private val requests = Channel<Unit>(Channel.CONFLATED)

    init {
        scope.launch {
            combine(
                authRepository.currentUser.map { it?.id }.distinctUntilChanged(),
                requests.receiveAsFlow().onStart { emit(Unit) },
            ) { userId, _ -> userId }
                .collect { userId -> if (userId != null) syncQuietly(userId) }
        }
    }

    override fun requestSync() {
        requests.trySend(Unit)
    }

    override suspend fun syncNow(userId: String) = sync.sync(userId)

    override suspend fun clearLocal(userId: String) = sync.clearLocal(userId)

    private suspend fun syncQuietly(userId: String) {
        try {
            sync.sync(userId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sem internet é o caso comum em campo; tenta de novo no próximo pedido.
            logWarning(TAG, "Sincronização falhou", e)
        }
    }
}
