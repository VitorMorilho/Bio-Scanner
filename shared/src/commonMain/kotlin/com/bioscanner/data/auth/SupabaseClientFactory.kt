package com.bioscanner.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

// Usado só quando faltam as credenciais: o cliente precisa de uma URL válida para ser criado, mas
// nenhuma chamada chega a sair (SupabaseAuthRepository barra antes com NOT_CONFIGURED).
private const val UNCONFIGURED_URL = "https://not-configured.supabase.co"

fun createBioScannerSupabaseClient(config: SupabaseConfig): SupabaseClient = createSupabaseClient(
    supabaseUrl = config.url.ifBlank { UNCONFIGURED_URL },
    supabaseKey = config.anonKey.ifBlank { "not-configured" },
) {
    // A sessão é salva e renovada pelo plugin (SharedPreferences no Android, NSUserDefaults no iOS).
    install(Auth)
    install(Functions)
    // Registros na nuvem (tabela observations + bucket de fotos), ver data/sync.
    install(Postgrest)
    install(Storage)
}
