package com.bioscanner.presentation.auth

import com.bioscanner.domain.auth.AuthError
import com.bioscanner.domain.auth.AuthException
import com.bioscanner.domain.auth.AuthRepository
import com.bioscanner.domain.auth.AuthUser
import com.bioscanner.domain.auth.SignUpResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private class FakeAuthRepository : AuthRepository {
        override val currentUser = MutableStateFlow<AuthUser?>(null)
        val calls = mutableListOf<String>()
        var failure: AuthError? = null
        var signUpResult = SignUpResult.NEEDS_EMAIL_CONFIRMATION

        private fun record(call: String) {
            calls += call
            failure?.let { throw AuthException(it) }
        }

        private fun signInAs(email: String) {
            currentUser.value = AuthUser(id = "u1", email = email, displayName = "Ana")
        }

        override suspend fun signUp(name: String, email: String, password: String): SignUpResult {
            record("signUp:$name:$email")
            if (signUpResult == SignUpResult.SIGNED_IN) signInAs(email)
            return signUpResult
        }
        override suspend fun confirmSignUp(email: String, code: String) {
            record("confirm:$email:$code")
            signInAs(email)
        }
        override suspend fun resendSignUpCode(email: String) = record("resend:$email")
        override suspend fun signIn(email: String, password: String) {
            record("signIn:$email")
            signInAs(email)
        }
        override suspend fun signInWithGoogle(idToken: String, rawNonce: String) {
            record("google:$idToken:$rawNonce")
            signInAs("ana@gmail.com")
        }
        override suspend fun sendPasswordResetCode(email: String) = record("reset:$email")
        override suspend fun resetPassword(email: String, code: String, newPassword: String) {
            record("newPassword:$email:$code")
            signInAs(email)
        }
        override suspend fun signOut() = record("signOut")
        override suspend fun deleteAccount() = record("delete")
    }

    private val repository = FakeAuthRepository()
    private lateinit var viewModel: AuthViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AuthViewModel(repository)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val state get() = viewModel.uiState.value

    @Test
    fun `invalid sign up shows field errors and does not call the server`() {
        viewModel.goTo(AuthStep.SIGN_UP)
        viewModel.onEmailChange("ana")
        viewModel.onPasswordChange("123")

        viewModel.submit()

        assertNotNull(state.nameError)
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun `sign up needing confirmation moves to the code step and confirming signs in`() = runTest {
        val done = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.done.first() }
        viewModel.goTo(AuthStep.SIGN_UP)
        viewModel.onNameChange("Ana")
        viewModel.onEmailChange("ana@x.com")
        viewModel.onPasswordChange("senha-segura-1")

        viewModel.submit()

        assertEquals(AuthStep.CONFIRM_SIGN_UP, state.step)
        assertEquals("ana@x.com", state.email)
        assertEquals("", state.password)
        assertNotNull(state.info)

        viewModel.onCodeChange("12 34-56")
        assertEquals("123456", state.code)
        viewModel.submit()

        assertEquals(listOf("signUp:Ana:ana@x.com", "confirm:ana@x.com:123456"), repository.calls)
        assertTrue(done.isCompleted)
    }

    @Test
    fun `server error is shown and loading ends`() {
        repository.failure = AuthError.INVALID_CREDENTIALS
        viewModel.onEmailChange("ana@x.com")
        viewModel.onPasswordChange("errada")

        viewModel.submit()

        assertEquals(AuthError.INVALID_CREDENTIALS.message(), state.error)
        assertFalse(state.loading)
    }

    @Test
    fun `sign in does not enforce the minimum password length`() {
        viewModel.onEmailChange("ana@x.com")
        viewModel.onPasswordChange("curta")

        viewModel.submit()

        assertNull(state.passwordError)
        assertEquals(listOf("signIn:ana@x.com"), repository.calls)
    }

    @Test
    fun `password reset sends a code then sets the new password`() {
        viewModel.goTo(AuthStep.FORGOT_PASSWORD)
        viewModel.onEmailChange("ana@x.com")
        viewModel.submit()

        assertEquals(AuthStep.RESET_PASSWORD, state.step)

        viewModel.onCodeChange("654321")
        viewModel.onPasswordChange("nova-senha-1")
        viewModel.submit()

        assertEquals(listOf("reset:ana@x.com", "newPassword:ana@x.com:654321"), repository.calls)
    }

    @Test
    fun `google cancel is silent and success signs in with the raw nonce`() {
        viewModel.onGoogleResult(GoogleSignInResult.Cancelled)
        assertNull(state.error)

        viewModel.onGoogleResult(GoogleSignInResult.Success(idToken = "tok", rawNonce = "raw"))

        assertEquals(listOf("google:tok:raw"), repository.calls)
    }

    @Test
    fun `back walks the steps and reports when the screen should close`() {
        viewModel.goTo(AuthStep.FORGOT_PASSWORD)
        viewModel.goTo(AuthStep.RESET_PASSWORD)

        assertTrue(viewModel.back())
        assertEquals(AuthStep.FORGOT_PASSWORD, state.step)
        assertTrue(viewModel.back())
        assertEquals(AuthStep.SIGN_IN, state.step)
        assertFalse(viewModel.back())
    }
}
