package com.bioscanner.presentation.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioscanner.domain.auth.AuthError
import com.bioscanner.domain.auth.AuthException
import com.bioscanner.domain.auth.AuthRepository
import com.bioscanner.domain.auth.AuthUser
import com.bioscanner.domain.model.Observation
import com.bioscanner.domain.model.regionNameAt
import com.bioscanner.domain.repository.ObservationRepository
import com.bioscanner.domain.repository.RegionRepository
import com.bioscanner.domain.usecase.DeleteAccountUseCase
import com.bioscanner.domain.usecase.SignOutResult
import com.bioscanner.domain.usecase.SignOutUseCase
import com.bioscanner.presentation.auth.message
import com.bioscanner.presentation.platform.logError
import com.bioscanner.presentation.platform.logWarning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "RecordsViewModel"

sealed interface AccountState {
    /** Sessão salva ainda sendo carregada: não mostra "Entrar" nem o nome. */
    data object Loading : AccountState
    data object SignedOut : AccountState
    data class SignedIn(val user: AuthUser) : AccountState
}

data class RecordItem(val observation: Observation, val regionName: String?)

data class RecordsUiState(
    val loading: Boolean = true,
    val account: AccountState = AccountState.Loading,
    /** Saindo ou excluindo a conta. */
    val accountBusy: Boolean = false,
    val accountMessage: String? = null,
    /** Registros da conta que ainda não subiram para a nuvem. */
    val pendingUploads: Int = 0,
    /** Não null = o logout parou porque [pendingUploads] registros se perderiam; pedir confirmação. */
    val signOutBlockedBy: Int? = null,
    val records: List<RecordItem> = emptyList(),
) {
    val withLocationCount: Int get() = records.count { it.observation.metadata.location != null }
    val distinctRegionsCount: Int get() = records.mapNotNull { it.regionName }.distinct().size
}

private data class AccountAction(
    val busy: Boolean = false,
    val message: String? = null,
    val signOutBlockedBy: Int? = null,
)

class RecordsViewModel(
    observationRepository: ObservationRepository,
    regionRepository: RegionRepository,
    private val authRepository: AuthRepository,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val accountAction = MutableStateFlow(AccountAction())

    private val records = observationRepository.observeAll().map { observations ->
        val regions = regionRepository.getRegionCatalog().regions
        observations.map { o -> RecordItem(o, o.metadata.location?.let { regions.regionNameAt(it) }) }
    }

    private val account = authRepository.currentUser
        .map { user -> if (user == null) AccountState.SignedOut else AccountState.SignedIn(user) }
        // A lista não espera a sessão: só o cartão do usuário fica em "carregando".
        .onStart<AccountState> { emit(AccountState.Loading) }

    val uiState: StateFlow<RecordsUiState> = combine(
        records,
        account,
        accountAction,
        observationRepository.observePendingUploadCount(),
    ) { records, account, action, pendingUploads ->
        RecordsUiState(
            loading = false,
            account = account,
            accountBusy = action.busy,
            accountMessage = action.message,
            pendingUploads = pendingUploads,
            signOutBlockedBy = action.signOutBlockedBy,
            records = records,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordsUiState())

    /** Sai da conta; se houver registros sem enviar, para e pede confirmação ([confirmSignOut]). */
    fun signOut() = runSignOut(force = false)

    /** Sai mesmo perdendo os registros que não subiram. */
    fun confirmSignOut() = runSignOut(force = true)

    fun cancelSignOut() = accountAction.update { it.copy(signOutBlockedBy = null) }

    private fun runSignOut(force: Boolean) = runAccountAction(successMessage = null) {
        val result = signOutUseCase(force)
        if (result is SignOutResult.HasPendingUploads) {
            accountAction.update { it.copy(signOutBlockedBy = result.count) }
        }
    }

    fun deleteAccount() = runAccountAction(successMessage = "Conta excluída. Seus dados foram apagados.") {
        deleteAccountUseCase()
    }

    fun dismissAccountMessage() = accountAction.update { it.copy(message = null) }

    private fun runAccountAction(successMessage: String?, block: suspend () -> Unit) {
        if (accountAction.value.busy) return
        accountAction.value = AccountAction(busy = true)
        viewModelScope.launch {
            val message = try {
                block()
                successMessage
            } catch (e: AuthException) {
                logWarning(TAG, "Ação de conta falhou", e)
                e.error.message()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Ex.: a conta já foi excluída no servidor, mas apagar os arquivos locais falhou.
                logError(TAG, "Ação de conta falhou", e)
                AuthError.UNKNOWN.message()
            }
            accountAction.update { it.copy(busy = false, message = message) }
        }
    }
}
