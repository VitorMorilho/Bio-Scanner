package com.bioscanner.presentation.auth

import com.bioscanner.domain.auth.AuthError
import com.bioscanner.domain.auth.CredentialsIssue
import com.bioscanner.domain.auth.MAX_NAME_LENGTH
import com.bioscanner.domain.auth.MIN_PASSWORD_LENGTH

fun AuthError.message(): String = when (this) {
    AuthError.INVALID_CREDENTIALS -> "Email ou senha incorretos."
    AuthError.EMAIL_NOT_CONFIRMED -> "Confirme seu email com o código que enviamos antes de entrar."
    AuthError.EMAIL_ALREADY_REGISTERED -> "Já existe uma conta com este email. Tente entrar."
    AuthError.WEAK_PASSWORD -> "Senha fraca. Use pelo menos $MIN_PASSWORD_LENGTH caracteres, misturando letras e números."
    AuthError.INVALID_OR_EXPIRED_CODE -> "Código inválido ou expirado. Confira o email ou peça um novo código."
    AuthError.RATE_LIMITED -> "Muitas tentativas seguidas. Aguarde alguns minutos e tente de novo."
    AuthError.NETWORK -> "Sem conexão com a internet. Verifique a rede e tente de novo."
    AuthError.NOT_CONFIGURED -> "Login indisponível nesta versão do app."
    AuthError.UNKNOWN -> "Algo deu errado. Tente novamente."
}

fun CredentialsIssue.message(): String = when (this) {
    CredentialsIssue.NAME_BLANK -> "Informe seu nome."
    CredentialsIssue.NAME_TOO_LONG -> "Use no máximo $MAX_NAME_LENGTH caracteres."
    CredentialsIssue.EMAIL_INVALID -> "Informe um email válido."
    CredentialsIssue.PASSWORD_TOO_SHORT -> "A senha precisa ter pelo menos $MIN_PASSWORD_LENGTH caracteres."
    CredentialsIssue.PASSWORD_NEEDS_LETTERS_AND_DIGITS -> "Use letras e números na senha."
    CredentialsIssue.CODE_INVALID -> "Digite o código numérico recebido por email."
}
