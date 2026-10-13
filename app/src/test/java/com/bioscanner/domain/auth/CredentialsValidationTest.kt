package com.bioscanner.domain.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CredentialsValidationTest {

    @Test
    fun `name must not be blank or too long`() {
        assertEquals(CredentialsIssue.NAME_BLANK, validateName("   "))
        assertEquals(CredentialsIssue.NAME_TOO_LONG, validateName("a".repeat(MAX_NAME_LENGTH + 1)))
        assertNull(validateName("  Ana  "))
    }

    @Test
    fun `email needs user, at sign and domain`() {
        assertNull(validateEmail(" ana@exemplo.com.br "))
        listOf("", "ana", "ana@", "ana@exemplo", "a na@exemplo.com").forEach {
            assertEquals(it, CredentialsIssue.EMAIL_INVALID, validateEmail(it))
        }
    }

    @Test
    fun `password has a minimum length`() {
        assertEquals(CredentialsIssue.PASSWORD_TOO_SHORT, validatePassword("a".repeat(MIN_PASSWORD_LENGTH - 1)))
        assertNull(validatePassword("a".repeat(MIN_PASSWORD_LENGTH)))
    }

    @Test
    fun `code is 6 to 10 digits`() {
        assertNull(validateCode("123456"))
        assertNull(validateCode(" 1234567890 "))
        listOf("12345", "12345678901", "12a456", "").forEach {
            assertEquals(it, CredentialsIssue.CODE_INVALID, validateCode(it))
        }
    }
}
