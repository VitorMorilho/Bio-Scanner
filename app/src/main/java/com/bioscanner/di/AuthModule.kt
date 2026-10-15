package com.bioscanner.di

import com.bioscanner.BuildConfig
import com.bioscanner.data.auth.GoogleCredentialCleaner
import com.bioscanner.data.auth.SupabaseAuthRepository
import com.bioscanner.data.auth.SupabaseConfig
import com.bioscanner.data.auth.createBioScannerSupabaseClient
import com.bioscanner.domain.auth.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideSupabaseConfig(): SupabaseConfig =
        SupabaseConfig(url = BuildConfig.SUPABASE_URL, anonKey = BuildConfig.SUPABASE_ANON_KEY)

    @Provides
    @Singleton
    fun provideSupabaseClient(config: SupabaseConfig): SupabaseClient = createBioScannerSupabaseClient(config)

    @Provides
    @Singleton
    fun provideAuthRepository(
        client: SupabaseClient,
        config: SupabaseConfig,
        googleCredentials: GoogleCredentialCleaner,
    ): AuthRepository = SupabaseAuthRepository(client, config, googleCredentials)
}
