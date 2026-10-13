package com.bioscanner.di

import com.bioscanner.domain.auth.AuthRepository
import com.bioscanner.domain.auth.AuthUser
import com.bioscanner.domain.auth.SignUpResult
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

/**
 * Substitui [AuthModule] nos testes instrumentados: sem rede nem Supabase, o usuário fica sempre
 * deslogado. O fluxo de conta em si é coberto pelos testes unitários do AuthViewModel.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [AuthModule::class])
object TestAuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository = object : AuthRepository {
        override val currentUser: Flow<AuthUser?> = flowOf(null)
        override suspend fun signUp(name: String, email: String, password: String) = SignUpResult.NEEDS_EMAIL_CONFIRMATION
        override suspend fun confirmSignUp(email: String, code: String) = Unit
        override suspend fun resendSignUpCode(email: String) = Unit
        override suspend fun signIn(email: String, password: String) = Unit
        override suspend fun signInWithGoogle(idToken: String, rawNonce: String) = Unit
        override suspend fun sendPasswordResetCode(email: String) = Unit
        override suspend fun resetPassword(email: String, code: String, newPassword: String) = Unit
        override suspend fun signOut() = Unit
        override suspend fun deleteAccount() = Unit
    }
}
