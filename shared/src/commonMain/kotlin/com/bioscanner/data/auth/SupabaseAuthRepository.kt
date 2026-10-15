package com.bioscanner.data.auth

import com.bioscanner.domain.auth.AuthError
import com.bioscanner.domain.auth.AuthException
import com.bioscanner.domain.auth.AuthRepository
import com.bioscanner.domain.auth.AuthUser
import com.bioscanner.domain.auth.SignUpResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonObject
import kotlinx.io.IOException

private const val DELETE_ACCOUNT_FUNCTION = "delete-account"

class SupabaseAuthRepository(
    private val supabase: SupabaseClient,
    private val config: SupabaseConfig,
    private val platformCredentials: PlatformCredentialsCleaner,
) : AuthRepository {

    private val auth get() = supabase.auth

    override val currentUser: Flow<AuthUser?> = auth.sessionStatus
        // Enquanto a sessão salva é carregada não sabemos se há login; não emitir evita a UI
        // piscar "sem conta" na abertura do app.
        .filterNot { it is SessionStatus.Initializing }
        .map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.user?.toAuthUser()
                // Falha ao renovar o token (ex.: sem internet em campo): a sessão continua salva
                // e é renovada quando a rede voltar, então o usuário segue logado.
                is SessionStatus.RefreshFailure -> auth.currentUserOrNull()?.toAuthUser()
                else -> null
            }
        }
        .distinctUntilChanged()

    override suspend fun signUp(name: String, email: String, password: String): SignUpResult = call {
        auth.signUpWith(Email) {
            this.email = email.normalizedEmail()
            this.password = password
            // Lido pelo trigger handle_new_user para criar o perfil (supabase/migrations).
            data = buildJsonObject { put(METADATA_DISPLAY_NAME, name.trim()) }
        }
        if (auth.currentSessionOrNull() != null) SignUpResult.SIGNED_IN else SignUpResult.NEEDS_EMAIL_CONFIRMATION
    }

    override suspend fun confirmSignUp(email: String, code: String) = call {
        auth.verifyEmailOtp(type = OtpType.Email.EMAIL, email = email.normalizedEmail(), token = code.trim())
        Unit
    }

    override suspend fun resendSignUpCode(email: String) = call {
        auth.resendEmail(OtpType.Email.SIGNUP, email.normalizedEmail())
    }

    override suspend fun signIn(email: String, password: String) = call {
        auth.signInWith(Email) {
            this.email = email.normalizedEmail()
            this.password = password
        }
    }

    override suspend fun signInWithGoogle(idToken: String, rawNonce: String) = call {
        auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
            nonce = rawNonce
        }
    }

    override suspend fun sendPasswordResetCode(email: String) = call {
        auth.resetPasswordForEmail(email.normalizedEmail())
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String) = call {
        auth.verifyEmailOtp(type = OtpType.Email.RECOVERY, email = email.normalizedEmail(), token = code.trim())
        auth.updateUser { password = newPassword }
        Unit
    }

    override suspend fun signOut() = call {
        try {
            auth.signOut()
        } catch (e: IOException) {
            // Sem internet não dá para revogar o token no servidor, mas sair do aparelho
            // não pode depender de rede. O refresh token expira sozinho no servidor.
            auth.clearSession()
        }
        platformCredentials.clear()
    }

    override suspend fun deleteAccount() = call {
        supabase.functions.invoke(DELETE_ACCOUNT_FUNCTION)
        // A conta não existe mais; chamar logout no servidor só daria erro.
        auth.clearSession()
        platformCredentials.clear()
    }

    private suspend fun <T> call(block: suspend () -> T): T {
        if (!config.isConfigured) throw AuthException(AuthError.NOT_CONFIGURED)
        return try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AuthException(e.toAuthError(), e)
        }
    }
}

private const val METADATA_DISPLAY_NAME = "display_name"

private fun String.normalizedEmail() = trim().lowercase()

/** Mesma ordem de preferência do trigger handle_new_user no servidor. */
internal fun UserInfo.toAuthUser(): AuthUser {
    val metadataName = listOf(METADATA_DISPLAY_NAME, "full_name", "name")
        .firstNotNullOfOrNull { key -> userMetadata?.get(key)?.jsonPrimitive?.contentOrNull?.trim()?.ifEmpty { null } }
    return AuthUser(
        id = id,
        email = email,
        displayName = metadataName ?: email?.substringBefore('@')?.ifEmpty { null } ?: "Usuário",
    )
}

internal fun Exception.toAuthError(): AuthError = when (this) {
    is AuthWeakPasswordException -> AuthError.WEAK_PASSWORD
    is AuthRestException -> when (errorCode) {
        AuthErrorCode.InvalidCredentials -> AuthError.INVALID_CREDENTIALS
        AuthErrorCode.EmailNotConfirmed -> AuthError.EMAIL_NOT_CONFIRMED
        AuthErrorCode.UserAlreadyExists, AuthErrorCode.EmailExists -> AuthError.EMAIL_ALREADY_REGISTERED
        AuthErrorCode.WeakPassword -> AuthError.WEAK_PASSWORD
        // O Supabase usa otp_expired tanto para código vencido quanto errado.
        AuthErrorCode.OtpExpired -> AuthError.INVALID_OR_EXPIRED_CODE
        AuthErrorCode.OverRequestRateLimit, AuthErrorCode.OverEmailSendRateLimit -> AuthError.RATE_LIMITED
        else -> AuthError.UNKNOWN
    }
    // HttpRequestException do supabase-kt e HttpRequestTimeoutException do Ktor são IOException
    // (kotlinx.io.IOException é a java.io.IOException na JVM).
    is IOException -> AuthError.NETWORK
    else -> AuthError.UNKNOWN
}
