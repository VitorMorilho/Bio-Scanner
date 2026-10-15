package com.bioscanner.presentation.platform

/**
 * Configuração que cada plataforma passa às telas (no Android vem do local.properties via
 * BuildConfig). Vazio/false esconde o recurso correspondente.
 */
data class AppConfig(
    /** Client ID do OAuth Client tipo Web do Google; vazio = sem botão "Continuar com Google". */
    val googleWebClientId: String = "",
    /** true só com SMTP próprio no Supabase: habilita "Esqueci minha senha" (ver supabase/README.md). */
    val emailCodesEnabled: Boolean = false,
)
