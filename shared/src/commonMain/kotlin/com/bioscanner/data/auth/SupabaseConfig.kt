package com.bioscanner.data.auth

/** No Android vem do local.properties (supabase.url / supabase.anonKey) via BuildConfig. */
data class SupabaseConfig(val url: String, val anonKey: String) {
    // Sem credenciais o app continua funcionando; só as ações de conta falham com NOT_CONFIGURED.
    val isConfigured: Boolean get() = url.isNotBlank() && anonKey.isNotBlank()
}
