package com.bioscanner.di

import com.bioscanner.data.repository.SpeciesRepositoryImpl
import com.bioscanner.domain.repository.SpeciesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSpeciesRepository(impl: SpeciesRepositoryImpl): SpeciesRepository
}
