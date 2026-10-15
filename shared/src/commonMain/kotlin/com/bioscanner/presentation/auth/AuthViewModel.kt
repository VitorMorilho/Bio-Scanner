package com.bioscanner.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioscanner.domain.auth.AuthError
import com.bioscanner.domain.auth.AuthException
import com.bioscanner.domain.auth.AuthRepository
import com.bioscanner.domain.auth.SignUpResult
import com.bioscanner.domain.auth.validateCode
import com.bioscanner.domain.auth.validateEmail
import com.bioscanner.domain.auth.validateName
import com.bioscanner.domain.auth.validatePassword
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Etapas da tela de conta. Uma tela só, para não espalhar email/senha por várias rotas. */
enum class AuthStep { SIGN_IN, SIGN_UP, CONFIRM_SIGN_UP, FORGOT_PASSWORD, RESET_PASSWORD }

data class AuthUiState(
    val step: AuthStep = AuthStep.SIGN_IN,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val code: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val codeError: String? = null,
    /** Erro geral (vindo do servidor). */
    val error: String? = null,
    /** Aviso neutro, ex.: "enviamos um código para…". */
    val info: String? = null,
    val loading: Boolean = false,
)

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    // Channel em vez de SharedFlow: se o login concluir durante uma rotação (sem ninguém
    // coletando), o evento fica guardado até a tela voltar, em vez de se perder.
    private val _done = Channel<Unit>(Channel.CONFLATED)

    /** Emite quando há um usuário logado: vale para email, código, nova senha e Google. */
    val done: Flow<Unit> = _done.receiveAsFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.filterNotNull().first()
            _done.trySend(Unit)
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, passwordError = null) }

    // Só dígitos: o teclado numérico ainda permite colar texto qualquer.
    fun onCodeChange(value: String) = _uiState.update { it.copy(code = value.filter(Char::isDigit), codeError = null) }

    /** Troca de etapa mantendo nome/email digitados; limpa senha, código e mensagens. */
    fun goTo(step: AuthStep) = _uiState.update {
        it.copy(
            step = step, password = "", code = "",
            nameError = null, emailError = null, passwordError = null, codeError = null, error = null, info = null,
        )
    }

    /** Voltar dentro da tela. false = já está na primeira etapa (a tela deve fechar). */
    fun back(): Boolean {
        val previous = when (_uiState.value.step) {
            AuthStep.SIGN_IN -> return false
            AuthStep.SIGN_UP, AuthStep.FORGOT_PASSWORD -> AuthStep.SIGN_IN
            AuthStep.CONFIRM_SIGN_UP -> AuthStep.SIGN_UP
            AuthStep.RESET_PASSWORD -> AuthStep.FORGOT_PASSWORD
        }
        goTo(previous)
        return true
    }

    fun submit() {
        val s = _uiState.value
        when (s.step) {
            AuthStep.SIGN_IN -> if (validate(email = true, passwordRequired = true)) {
                launchAuth { authRepository.signIn(s.email, s.password) }
            }
            AuthStep.SIGN_UP -> if (validate(name = true, email = true, password = true)) {
                launchAuth {
                    when (authRepository.signUp(s.name, s.email, s.password)) {
                        SignUpResult.SIGNED_IN -> Unit // done chega pelo currentUser
                        SignUpResult.NEEDS_EMAIL_CONFIRMATION -> {
                            goTo(AuthStep.CONFIRM_SIGN_UP)
                            _uiState.update { it.copy(info = "Enviamos um código para ${s.email.trim()}.") }
                        }
                    }
                }
            }
            AuthStep.CONFIRM_SIGN_UP -> if (validate(code = true)) {
                launchAuth { authRepository.confirmSignUp(s.email, s.code) }
            }
            AuthStep.FORGOT_PASSWORD -> if (validate(email = true)) {
                launchAuth {
                    authRepository.sendPasswordResetCode(s.email)
                    goTo(AuthStep.RESET_PASSWORD)
                    // Mesmo texto exista a conta ou não: o Supabase não revela quais emails têm conta.
                    _uiState.update { it.copy(info = "Se houver uma conta com ${s.email.trim()}, você vai receber um código.") }
                }
            }
            AuthStep.RESET_PASSWORD -> if (validate(code = true, password = true)) {
                launchAuth { authRepository.resetPassword(s.email, s.code, s.password) }
            }
        }
    }

    fun resendSignUpCode() = launchAuth {
        authRepository.resendSignUpCode(_uiState.value.email)
        _uiState.update { it.copy(info = "Enviamos um novo código.") }
    }

    fun onGoogleResult(result: GoogleSignInResult) {
        when (result) {
            is GoogleSignInResult.Success -> launchAuth { authRepository.signInWithGoogle(result.idToken, result.rawNonce) }
            GoogleSignInResult.Cancelled -> Unit
            GoogleSignInResult.NoAccount -> _uiState.update {
                it.copy(error = "Nenhuma conta Google neste aparelho. Adicione uma nas configurações ou use email e senha.")
            }
            GoogleSignInResult.Failed -> _uiState.update { it.copy(error = AuthError.UNKNOWN.message()) }
        }
    }

    fun onGoogleStarted() = _uiState.update { it.copy(error = null, info = null) }

    private fun validate(
        name: Boolean = false,
        email: Boolean = false,
        password: Boolean = false,
        passwordRequired: Boolean = false,
        code: Boolean = false,
    ): Boolean {
        val s = _uiState.value
        val nameError = if (name) validateName(s.name)?.message() else null
        val emailError = if (email) validateEmail(s.email)?.message() else null
        // No login não aplicamos o tamanho mínimo: contas antigas podem ter outra regra.
        val passwordError = when {
            password -> validatePassword(s.password)?.message()
            passwordRequired && s.password.isEmpty() -> "Informe sua senha."
            else -> null
        }
        val codeError = if (code) validateCode(s.code)?.message() else null
        _uiState.update {
            it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError, codeError = codeError)
        }
        return listOfNotNull(nameError, emailError, passwordError, codeError).isEmpty()
    }

    private fun launchAuth(block: suspend () -> Unit) {
        if (_uiState.value.loading) return
        _uiState.update { it.copy(loading = true, error = null, info = null) }
        viewModelScope.launch {
            try {
                block()
            } catch (e: AuthException) {
                _uiState.update { it.copy(error = e.error.message()) }
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }
}
