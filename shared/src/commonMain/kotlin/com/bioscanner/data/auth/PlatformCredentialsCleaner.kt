package com.bioscanner.data.auth

/**
 * Esquece credenciais que a plataforma guardou fora do Supabase ao sair ou excluir a conta
 * (no Android, a conta Google escolhida no Credential Manager). Falhas não devem ser lançadas:
 * sair da conta não pode depender disso.
 */
fun interface PlatformCredentialsCleaner {
    suspend fun clear()

    companion object {
        /** Para plataformas sem login social ainda (iOS). */
        val None = PlatformCredentialsCleaner {}
    }
}
