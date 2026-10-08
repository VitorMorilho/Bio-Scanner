package com.bioscanner.di

import com.bioscanner.data.observation.AndroidCaptureMetadataProvider
import com.bioscanner.data.observation.InternalPhotoStorage
import com.bioscanner.data.repository.ObservationRepositoryImpl
import com.bioscanner.domain.observation.CaptureMetadataProvider
import com.bioscanner.domain.observation.PhotoStorage
import com.bioscanner.domain.repository.ObservationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ObservationModule {

    @Binds
    @Singleton
    abstract fun bindObservationRepository(impl: ObservationRepositoryImpl): ObservationRepository

    @Binds
    abstract fun bindCaptureMetadataProvider(impl: AndroidCaptureMetadataProvider): CaptureMetadataProvider

    @Binds
    abstract fun bindPhotoStorage(impl: InternalPhotoStorage): PhotoStorage
}
