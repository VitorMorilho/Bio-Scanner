package com.bioscanner.domain.auth

// Mesmas regras do Supabase (supabase/config.toml: minimum_password_length e password_requirements).
const val MIN_PASSWORD_LENGTH = 8
const val MAX_NAME_LENGTH = 100

// Validação simples de formato; quem garante que o email existe é o código de confirmação.
private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

// O Supabase gera códigos de 6 a 10 dígitos, conforme "Email OTP Length" no painel.
private val CODE_REGEX = Regex("^\\d{6,10}$")

enum class CredentialsIssue { NAME_BLANK, NAME_TOO_LONG, EMAIL_INVALID, PASSWORD_TOO_SHORT, PASSWORD_NEEDS_LETTERS_AND_DIGITS, CODE_INVALID }

fun validateName(name: String): CredentialsIssue? = when {
    name.isBlank() -> CredentialsIssue.NAME_BLANK
    name.trim().length > MAX_NAME_LENGTH -> CredentialsIssue.NAME_TOO_LONG
    else -> null
}

fun validateEmail(email: String): CredentialsIssue? =
    if (EMAIL_REGEX.matches(email.trim())) null else CredentialsIssue.EMAIL_INVALID

fun validatePassword(password: String): CredentialsIssue? = when {
    password.length < MIN_PASSWORD_LENGTH -> CredentialsIssue.PASSWORD_TOO_SHORT
    password.none { it.isLetter() } || password.none { it.isDigit() } -> CredentialsIssue.PASSWORD_NEEDS_LETTERS_AND_DIGITS
    else -> null
}

fun validateCode(code: String): CredentialsIssue? =
    if (CODE_REGEX.matches(code.trim())) null else CredentialsIssue.CODE_INVALID
