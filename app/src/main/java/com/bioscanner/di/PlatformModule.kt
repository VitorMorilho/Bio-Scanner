package com.bioscanner.di

import android.content.Context
import com.bioscanner.data.platform.AssetsResourceReader
import com.bioscanner.data.platform.ResourceReader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Implementações Android das portas de plataforma do módulo shared. Separado do
 * DatabaseModule porque os testes substituem o banco, mas não a leitura de assets.
 */
@Module
@InstallIn(SingletonComponent::class)
object PlatformModule {

    @Provides
    @Singleton
    fun provideResourceReader(@ApplicationContext context: Context): ResourceReader = AssetsResourceReader(context)
}
