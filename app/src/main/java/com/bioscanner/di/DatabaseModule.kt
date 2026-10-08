package com.bioscanner.di

import android.content.Context
import androidx.room.Room
import com.bioscanner.data.local.db.BioScannerDatabase
import com.bioscanner.data.local.db.DatabaseCallback
import com.bioscanner.data.local.db.MIGRATION_2_3
import com.bioscanner.data.local.db.ObservationDao
import com.bioscanner.data.local.db.SpeciesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Provider
import javax.inject.Singleton

private const val DATABASE_NAME = "bioscanner.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        databaseProvider: Provider<BioScannerDatabase>,
    ): BioScannerDatabase = Room.databaseBuilder(context, BioScannerDatabase::class.java, DATABASE_NAME)
        .addCallback(DatabaseCallback(context, scope, databaseProvider))
        .addMigrations(MIGRATION_2_3)
        // Só a v1 pode ser recriada do zero: ela tinha apenas o seed dos
        // assets. Da v2 em diante há registros do usuário → só Migration.
        .fallbackToDestructiveMigrationFrom(dropAllTables = true, 1)
        .build()

    @Provides
    fun provideSpeciesDao(database: BioScannerDatabase): SpeciesDao = database.speciesDao()

    @Provides
    fun provideObservationDao(database: BioScannerDatabase): ObservationDao = database.observationDao()
}
