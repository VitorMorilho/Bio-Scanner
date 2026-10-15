package com.bioscanner.presentation.auth

import androidx.compose.runtime.Composable

sealed interface GoogleSignInResult {
    data class Success(val idToken: String, val rawNonce: String) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data object NoAccount : GoogleSignInResult
    data object Failed : GoogleSignInResult
}

/**
 * Função que abre o seletor de contas do Google e devolve o ID token, ou null quando a
 * plataforma ainda não tem login com Google (o botão some).
 */
@Composable
expect fun rememberGoogleSignIn(webClientId: String): (suspend () -> GoogleSignInResult)?
