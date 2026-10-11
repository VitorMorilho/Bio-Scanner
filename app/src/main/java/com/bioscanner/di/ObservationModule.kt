package com.bioscanner.di

import com.bioscanner.data.observation.AndroidCaptureMetadataProvider
import com.bioscanner.data.observation.InternalPhotoStorage
import com.bioscanner.domain.observation.CaptureMetadataProvider
import com.bioscanner.domain.observation.PhotoStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ObservationModule {

    @Binds
    abstract fun bindCaptureMetadataProvider(impl: AndroidCaptureMetadataProvider): CaptureMetadataProvider

    @Binds
    abstract fun bindPhotoStorage(impl: InternalPhotoStorage): PhotoStorage
}
