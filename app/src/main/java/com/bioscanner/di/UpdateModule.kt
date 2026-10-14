package com.bioscanner.di

import com.bioscanner.BuildConfig
import com.bioscanner.data.update.GitHubUpdateChecker
import com.bioscanner.domain.update.UpdateChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UpdateModule {

    @Provides
    @Singleton
    fun provideUpdateChecker(): UpdateChecker = GitHubUpdateChecker(BuildConfig.UPDATE_REPOSITORY)
}
