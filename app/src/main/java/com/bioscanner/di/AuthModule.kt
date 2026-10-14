package com.bioscanner.di

import com.bioscanner.BuildConfig
import com.bioscanner.data.auth.SupabaseAuthRepository
import com.bioscanner.data.auth.SupabaseConfig
import com.bioscanner.domain.auth.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import javax.inject.Singleton

// Usado só quando o local.properties não tem as credenciais: o cliente precisa de uma URL válida
// para ser criado, mas nenhuma chamada chega a sair (SupabaseAuthRepository barra antes).
private const val UNCONFIGURED_URL = "https://not-configured.supabase.co"

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository

    companion object {
        @Provides
        @Singleton
        fun provideSupabaseConfig(): SupabaseConfig =
            SupabaseConfig(url = BuildConfig.SUPABASE_URL, anonKey = BuildConfig.SUPABASE_ANON_KEY)

        @Provides
        @Singleton
        fun provideSupabaseClient(config: SupabaseConfig): SupabaseClient = createSupabaseClient(
            supabaseUrl = config.url.ifBlank { UNCONFIGURED_URL },
            supabaseKey = config.anonKey.ifBlank { "not-configured" },
        ) {
            // A sessão é salva e renovada automaticamente pelo plugin (SharedPreferences no Android).
            install(Auth)
            install(Functions)
        }
    }
}
