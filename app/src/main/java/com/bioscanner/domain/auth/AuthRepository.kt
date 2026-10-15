package com.bioscanner.domain.auth

import kotlinx.coroutines.flow.Flow

/**
 * Conta do usuário. Todas as funções suspensas lançam [AuthException] em caso de falha.
 *
 * Confirmação de cadastro e recuperação de senha usam um código numérico enviado por email
 * (digitado no app), não link: assim não precisamos de deep link nem de página web.
 */
interface AuthRepository {
    /** null = sem login. */
    val currentUser: Flow<AuthUser?>

    suspend fun signUp(name: String, email: String, password: String): SignUpResult

    /** Conclui o cadastro com o código recebido por email; já deixa o usuário logado. */
    suspend fun confirmSignUp(email: String, code: String)

    suspend fun resendSignUpCode(email: String)

    suspend fun signIn(email: String, password: String)

    /** [rawNonce] é o nonce original; o Google recebeu o hash SHA-256 dele. */
    suspend fun signInWithGoogle(idToken: String, rawNonce: String)

    suspend fun sendPasswordResetCode(email: String)

    /** Valida o código de recuperação, define a nova senha e deixa o usuário logado. */
    suspend fun resetPassword(email: String, code: String, newPassword: String)

    suspend fun signOut()

    /** Apaga a conta no servidor (definitivo) e encerra a sessão local. */
    suspend fun deleteAccount()
}
