package com.bioscanner.di

import com.bioscanner.BuildConfig
import com.bioscanner.data.auth.GoogleCredentialCleaner
import com.bioscanner.data.auth.PlatformCredentialsCleaner
import com.bioscanner.data.auth.SupabaseConfig
import com.bioscanner.data.classifier.MockImageClassifier
import com.bioscanner.data.observation.AndroidCaptureMetadataProvider
import com.bioscanner.data.observation.InternalPhotoStorage
import com.bioscanner.data.platform.AssetsResourceReader
import com.bioscanner.data.platform.ResourceReader
import com.bioscanner.data.platform.SeedVersionStore
import com.bioscanner.data.platform.SharedPrefsSeedVersionStore
import com.bioscanner.data.platform.androidDatabaseBuilder
import com.bioscanner.data.update.GitHubUpdateChecker
import com.bioscanner.domain.classifier.ImageClassifier
import com.bioscanner.domain.observation.CaptureMetadataProvider
import com.bioscanner.domain.observation.PhotoStorage
import com.bioscanner.domain.update.UpdateChecker
import com.bioscanner.domain.usecase.ClassifyAndLookupSpeciesUseCase
import com.bioscanner.presentation.common.AndroidImageLoader
import com.bioscanner.presentation.common.ImageLoader
import com.bioscanner.presentation.platform.AppConfig
import com.bioscanner.presentation.update.UpdateViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Implementações Android das portas que o [sharedModule] espera. */
val androidPlatformModule = module {
    single { androidDatabaseBuilder(androidContext()) }
    single<ResourceReader> { AssetsResourceReader(androidContext()) }
    single<SeedVersionStore> { SharedPrefsSeedVersionStore(androidContext()) }
    single<PhotoStorage> { InternalPhotoStorage(androidContext()) }
    single<CaptureMetadataProvider> { AndroidCaptureMetadataProvider(androidContext()) }
    single { SupabaseConfig(url = BuildConfig.SUPABASE_URL, anonKey = BuildConfig.SUPABASE_ANON_KEY) }
    single<PlatformCredentialsCleaner> { GoogleCredentialCleaner(androidContext()) }
    single<ImageLoader> { AndroidImageLoader(androidContext()) }
    single {
        AppConfig(
            googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
            emailCodesEnabled = BuildConfig.EMAIL_CODES_ENABLED,
        )
    }
}

/**
 * Ponto de troca do classificador: quando houver modelo real, trocar MockImageClassifier por
 * TfLiteImageClassifier(androidContext()) aqui.
 */
val classifierModule = module {
    single<ImageClassifier> { MockImageClassifier(get()) }
    factoryOf(::ClassifyAndLookupSpeciesUseCase)
}

/** Aviso de atualização: só no Android (no iOS as atualizações vêm pela App Store/TestFlight). */
val updateModule = module {
    single<UpdateChecker> { GitHubUpdateChecker(BuildConfig.UPDATE_REPOSITORY) }
    viewModelOf(::UpdateViewModel)
}

val appModules = listOf(sharedModule, presentationModule, androidPlatformModule, classifierModule, updateModule)
