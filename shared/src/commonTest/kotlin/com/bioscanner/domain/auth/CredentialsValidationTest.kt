package com.bioscanner.domain.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CredentialsValidationTest {

    @Test
    fun `name must not be blank or too long`() {
        assertEquals(CredentialsIssue.NAME_BLANK, validateName("   "))
        assertEquals(CredentialsIssue.NAME_TOO_LONG, validateName("a".repeat(MAX_NAME_LENGTH + 1)))
        assertNull(validateName("  Ana  "))
    }

    @Test
    fun `email needs user at sign and domain`() {
        assertNull(validateEmail(" ana@exemplo.com.br "))
        listOf("", "ana", "ana@", "ana@exemplo", "a na@exemplo.com").forEach {
            assertEquals(CredentialsIssue.EMAIL_INVALID, validateEmail(it), it)
        }
    }

    @Test
    fun `password has a minimum length`() {
        assertEquals(CredentialsIssue.PASSWORD_TOO_SHORT, validatePassword("a".repeat(MIN_PASSWORD_LENGTH - 1)))
        assertEquals(CredentialsIssue.PASSWORD_NEEDS_LETTERS_AND_DIGITS, validatePassword("a".repeat(MIN_PASSWORD_LENGTH)))
        assertEquals(CredentialsIssue.PASSWORD_NEEDS_LETTERS_AND_DIGITS, validatePassword("1".repeat(MIN_PASSWORD_LENGTH)))
        assertNull(validatePassword("senha123"))
    }

    @Test
    fun `code is 6 to 10 digits`() {
        assertNull(validateCode("123456"))
        assertNull(validateCode(" 1234567890 "))
        listOf("12345", "12345678901", "12a456", "").forEach {
            assertEquals(CredentialsIssue.CODE_INVALID, validateCode(it), it)
        }
    }
}
