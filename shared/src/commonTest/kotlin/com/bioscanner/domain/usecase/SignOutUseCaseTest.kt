package com.bioscanner.domain.usecase

import com.bioscanner.domain.auth.AuthError
import com.bioscanner.domain.auth.AuthException
import com.bioscanner.domain.auth.AuthRepository
import com.bioscanner.domain.auth.AuthUser
import com.bioscanner.domain.auth.SignUpResult
import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.model.Observation
import com.bioscanner.domain.observation.ObservationSyncer
import com.bioscanner.domain.repository.ObservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SignOutUseCaseTest {

    private val events = mutableListOf<String>()
    private var pendingAfterSync = 0
    private var syncFailure: Exception? = null
    private var signOutFailure: AuthException? = null

    private val authRepository = object : AuthRepository {
        override val currentUser: Flow<AuthUser?> = flowOf(AuthUser(id = "user-1", email = "a@b.c", displayName = "A"))
        override suspend fun signUp(name: String, email: String, password: String) = SignUpResult.SIGNED_IN
        override suspend fun confirmSignUp(email: String, code: String) = Unit
        override suspend fun resendSignUpCode(email: String) = Unit
        override suspend fun signIn(email: String, password: String) = Unit
        override suspend fun signInWithGoogle(idToken: String, rawNonce: String) = Unit
        override suspend fun sendPasswordResetCode(email: String) = Unit
        override suspend fun resetPassword(email: String, code: String, newPassword: String) = Unit
        override suspend fun signOut() {
            signOutFailure?.let { throw it }
            events += "signOut"
        }
        override suspend fun deleteAccount() = Unit
    }

    private val repository = object : ObservationRepository {
        override fun observeAll(): Flow<List<Observation>> = emptyFlow()
        override fun observeById(id: Long): Flow<Observation?> = emptyFlow()
        override fun observePendingUploadCount(): Flow<Int> = flowOf(pendingAfterSync)
        override suspend fun create(photoPath: String, metadata: CaptureMetadata): Long = 0
        override suspend fun findById(id: Long): Observation? = null
        override suspend fun delete(id: Long) = Unit
        override suspend fun deleteAll() = Unit
    }

    private val syncer = object : ObservationSyncer {
        override fun requestSync() = Unit
        override suspend fun syncNow(userId: String) {
            events += "sync:$userId"
            syncFailure?.let { throw it }
        }
        override suspend fun clearLocal(userId: String) {
            events += "clear:$userId"
        }
    }

    private val useCase = SignOutUseCase(authRepository, repository, syncer)

    @Test
    fun `uploads what is pending then signs out and clears the device`() = runTest {
        assertEquals(SignOutResult.SignedOut, useCase())

        assertEquals(listOf("sync:user-1", "signOut", "clear:user-1"), events)
    }

    @Test
    fun `stops when records could not be uploaded`() = runTest {
        syncFailure = IOException("sem internet")
        pendingAfterSync = 2

        assertEquals(SignOutResult.HasPendingUploads(2), useCase())

        assertEquals(listOf("sync:user-1"), events)
    }

    @Test
    fun `force signs out even with pending records`() = runTest {
        pendingAfterSync = 2

        assertEquals(SignOutResult.SignedOut, useCase(force = true))

        assertEquals(listOf("signOut", "clear:user-1"), events)
    }

    @Test
    fun `failed sign out keeps the records on the device`() = runTest {
        signOutFailure = AuthException(AuthError.UNKNOWN)

        assertFailsWith<AuthException> { useCase() }

        assertEquals(listOf("sync:user-1"), events)
    }
}
