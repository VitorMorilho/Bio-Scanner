package com.bioscanner.presentation.auth

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.bioscanner.data.auth.GoogleNonce
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

private const val TAG = "GoogleSignIn"

@Composable
actual fun rememberGoogleSignIn(webClientId: String): (suspend () -> GoogleSignInResult)? {
    if (webClientId.isBlank()) return null
    // Contexto da Activity: o seletor de contas é exibido por cima dela.
    val context = LocalContext.current
    return remember(context, webClientId) { { requestGoogleIdToken(context, webClientId) } }
}

/**
 * Abre o seletor de contas do Google (Credential Manager) e devolve o ID token.
 * Precisa do contexto da Activity: o seletor é exibido por cima dela.
 */
private suspend fun requestGoogleIdToken(activityContext: Context, webClientId: String): GoogleSignInResult {
    val nonce = GoogleNonce.create()
    // GetSignInWithGoogleOption = fluxo do botão "Entrar com Google" (sempre mostra o seletor),
    // diferente do GetGoogleIdOption, pensado para login automático na abertura do app.
    val option = GetSignInWithGoogleOption.Builder(webClientId).setNonce(nonce.hashed).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
    return try {
        val credential = CredentialManager.create(activityContext).getCredential(activityContext, request).credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            GoogleSignInResult.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken, nonce.raw)
        } else {
            Log.e(TAG, "Credencial inesperada: ${credential.type}")
            GoogleSignInResult.Failed
        }
    } catch (e: GetCredentialCancellationException) {
        GoogleSignInResult.Cancelled
    } catch (e: NoCredentialException) {
        GoogleSignInResult.NoAccount
    } catch (e: GetCredentialException) {
        Log.e(TAG, "Falha no login com Google", e)
        GoogleSignInResult.Failed
    } catch (e: GoogleIdTokenParsingException) {
        Log.e(TAG, "ID token do Google inválido", e)
        GoogleSignInResult.Failed
    }
}
