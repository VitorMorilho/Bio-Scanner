package com.bioscanner.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import kotlinx.coroutines.CancellationException

private const val TAG = "GoogleCredentialCleaner"

/**
 * Esquece a conta Google escolhida no aparelho, para que o próximo login volte a mostrar o
 * seletor de contas em vez de entrar direto na mesma conta.
 */
class GoogleCredentialCleaner(
    private val context: Context,
) : PlatformCredentialsCleaner {
    override suspend fun clear() {
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Não impede sair nem excluir a conta: no pior caso o seletor sugere a conta anterior.
            Log.w(TAG, "Falha ao limpar a credencial do Google", e)
        }
    }
}
