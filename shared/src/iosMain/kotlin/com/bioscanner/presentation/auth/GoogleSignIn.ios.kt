package com.bioscanner.presentation.auth

import androidx.compose.runtime.Composable

// TODO(iOS): SDK GoogleSignIn do iOS (e Sign in with Apple, exigido pela App Store junto com Google).
@Composable
actual fun rememberGoogleSignIn(webClientId: String): (suspend () -> GoogleSignInResult)? = null
