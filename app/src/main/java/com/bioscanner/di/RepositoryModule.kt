package com.bioscanner.di

import com.bioscanner.data.local.db.ObservationDao
import com.bioscanner.data.local.db.SpeciesDao
import com.bioscanner.data.platform.ResourceReader
import com.bioscanner.data.repository.AssetRegionRepository
import com.bioscanner.data.repository.ObservationRepositoryImpl
import com.bioscanner.data.repository.SpeciesRepositoryImpl
import com.bioscanner.domain.observation.PhotoStorage
import com.bioscanner.domain.repository.ObservationRepository
import com.bioscanner.domain.repository.RegionRepository
import com.bioscanner.domain.repository.SpeciesRepository
import com.bioscanner.domain.usecase.DeleteObservationUseCase
import com.bioscanner.domain.usecase.GetSpeciesByIdUseCase
import com.bioscanner.domain.usecase.RegisterObservationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repositórios e casos de uso vêm do módulo shared (KMP), sem @Inject — por
 * isso são criados aqui com @Provides em vez de @Binds.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSpeciesRepository(dao: SpeciesDao): SpeciesRepository = SpeciesRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideRegionRepository(resources: ResourceReader, dao: SpeciesDao): RegionRepository =
        AssetRegionRepository(resources, dao)

    @Provides
    @Singleton
    fun provideObservationRepository(dao: ObservationDao): ObservationRepository =
        ObservationRepositoryImpl(dao, now = System::currentTimeMillis)

    @Provides
    fun provideGetSpeciesById(repository: SpeciesRepository) = GetSpeciesByIdUseCase(repository)

    @Provides
    fun provideRegisterObservation(photoStorage: PhotoStorage, repository: ObservationRepository) =
        RegisterObservationUseCase(photoStorage, repository)

    @Provides
    fun provideDeleteObservation(repository: ObservationRepository, photoStorage: PhotoStorage) =
        DeleteObservationUseCase(repository, photoStorage)
}
