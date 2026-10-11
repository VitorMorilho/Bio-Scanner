package com.bioscanner.di

import android.content.Context
import com.bioscanner.data.local.db.BioScannerDatabase
import com.bioscanner.data.local.db.ObservationDao
import com.bioscanner.data.local.db.SeedingCallback
import com.bioscanner.data.local.db.SpeciesDao
import com.bioscanner.data.local.db.configure
import com.bioscanner.data.local.seed.SpeciesSeedLoader
import com.bioscanner.data.platform.ResourceReader
import com.bioscanner.data.platform.SharedPrefsSeedVersionStore
import com.bioscanner.data.platform.androidDatabaseBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        resources: ResourceReader,
        databaseProvider: Provider<BioScannerDatabase>,
    ): BioScannerDatabase = androidDatabaseBuilder(context).configure(
        SeedingCallback(
            seedLoader = SpeciesSeedLoader(resources),
            versionStore = SharedPrefsSeedVersionStore(context),
            scope = scope,
            database = { databaseProvider.get() },
        ),
    )

    @Provides
    fun provideSpeciesDao(database: BioScannerDatabase): SpeciesDao = database.speciesDao()

    @Provides
    fun provideObservationDao(database: BioScannerDatabase): ObservationDao = database.observationDao()
}
