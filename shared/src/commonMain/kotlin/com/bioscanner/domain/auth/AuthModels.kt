package com.bioscanner.domain.auth

/** Usuário logado. Login é opcional: sem conta, o app funciona igual (registros ficam no aparelho). */
data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String,
)

/** Resultado do cadastro por email: com "Confirm email" ligado no Supabase, falta digitar o código. */
enum class SignUpResult { SIGNED_IN, NEEDS_EMAIL_CONFIRMATION }

/** Motivos de falha que a interface sabe explicar ao usuário. */
enum class AuthError {
    INVALID_CREDENTIALS,
    EMAIL_NOT_CONFIRMED,
    EMAIL_ALREADY_REGISTERED,
    WEAK_PASSWORD,
    INVALID_OR_EXPIRED_CODE,
    RATE_LIMITED,
    NETWORK,

    /** URL/anon key do Supabase ausentes no local.properties. */
    NOT_CONFIGURED,
    UNKNOWN,
}

class AuthException(val error: AuthError, cause: Throwable? = null) : Exception(error.name, cause)
