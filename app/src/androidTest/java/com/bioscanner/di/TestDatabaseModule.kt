package com.bioscanner.di

import android.content.Context
import androidx.room.Room
import com.bioscanner.data.local.db.BioScannerDatabase
import com.bioscanner.data.local.db.SpeciesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Substitui [DatabaseModule] nos testes instrumentados por um banco Room
 * em memória, sem o callback assíncrono de seeding — os testes semeiam os
 * dados que precisam explicitamente, de forma síncrona, para evitar
 * flakiness.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BioScannerDatabase =
        Room.inMemoryDatabaseBuilder(context, BioScannerDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Provides
    fun provideSpeciesDao(database: BioScannerDatabase): SpeciesDao = database.speciesDao()
}
