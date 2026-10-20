package com.bioscanner.domain.usecase

import com.bioscanner.domain.auth.AuthError
import com.bioscanner.domain.auth.AuthException
import com.bioscanner.domain.auth.AuthRepository
import com.bioscanner.domain.auth.AuthUser
import com.bioscanner.domain.auth.SignUpResult
import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.model.Observation
import com.bioscanner.domain.observation.PhotoStorage
import com.bioscanner.domain.repository.ObservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeleteAccountUseCaseTest {

    private val events = mutableListOf<String>()
    private var serverFailure: AuthException? = null

    private val authRepository = object : AuthRepository {
        override val currentUser: Flow<AuthUser?> = emptyFlow()
        override suspend fun signUp(name: String, email: String, password: String) = SignUpResult.SIGNED_IN
        override suspend fun confirmSignUp(email: String, code: String) = Unit
        override suspend fun resendSignUpCode(email: String) = Unit
        override suspend fun signIn(email: String, password: String) = Unit
        override suspend fun signInWithGoogle(idToken: String, rawNonce: String) = Unit
        override suspend fun sendPasswordResetCode(email: String) = Unit
        override suspend fun resetPassword(email: String, code: String, newPassword: String) = Unit
        override suspend fun signOut() = Unit
        override suspend fun deleteAccount() {
            serverFailure?.let { throw it }
            events += "server"
        }
    }

    private val observationRepository = object : ObservationRepository {
        override fun observeAll(): Flow<List<Observation>> = emptyFlow()
        override fun observeById(id: Long): Flow<Observation?> = emptyFlow()
        override fun observePendingUploadCount(): Flow<Int> = emptyFlow()
        override suspend fun create(photoPath: String, metadata: CaptureMetadata): Long = 0
        override suspend fun findById(id: Long): Observation? = null
        override suspend fun delete(id: Long) = Unit
        override suspend fun deleteAll() {
            events += "db"
        }
    }

    private val photoStorage = object : PhotoStorage {
        override suspend fun import(photoUri: String) = ""
        override suspend fun delete(photoPath: String) = Unit
        override suspend fun deleteAll() {
            events += "files"
        }
        override suspend fun readBytes(photoPath: String): ByteArray? = null
        override suspend fun save(bytes: ByteArray) = ""
    }

    private val useCase = DeleteAccountUseCase(authRepository, observationRepository, photoStorage)

    @Test
    fun `deletes the server account then local records then photos`() = runTest {
        useCase()

        assertEquals(listOf("server", "db", "files"), events)
    }

    @Test
    fun `server failure keeps local data`() = runTest {
        serverFailure = AuthException(AuthError.NETWORK)

        val thrown = assertFailsWith<AuthException> { useCase() }

        assertEquals(AuthError.NETWORK, thrown.error)
        assertEquals(emptyList<String>(), events)
    }
}
