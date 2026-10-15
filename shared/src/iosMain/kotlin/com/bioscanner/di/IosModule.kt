package com.bioscanner.di

import com.bioscanner.data.auth.PlatformCredentialsCleaner
import com.bioscanner.data.auth.SupabaseConfig
import com.bioscanner.data.observation.IosCaptureMetadataProvider
import com.bioscanner.data.observation.IosPhotoStorage
import com.bioscanner.data.platform.BundleResourceReader
import com.bioscanner.data.platform.ResourceReader
import com.bioscanner.data.platform.SeedVersionStore
import com.bioscanner.data.platform.UserDefaultsSeedVersionStore
import com.bioscanner.data.platform.iosDatabaseBuilder
import com.bioscanner.domain.observation.CaptureMetadataProvider
import com.bioscanner.domain.observation.PhotoStorage
import com.bioscanner.presentation.common.ImageLoader
import com.bioscanner.presentation.common.IosImageLoader
import com.bioscanner.presentation.platform.AppConfig
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.Foundation.NSBundle

/** Implementações iOS das portas que o [sharedModule] espera. */
val iosPlatformModule = module {
    single { iosDatabaseBuilder() }
    single<ResourceReader> { BundleResourceReader() }
    single<SeedVersionStore> { UserDefaultsSeedVersionStore() }
    single<PhotoStorage> { IosPhotoStorage() }
    single<CaptureMetadataProvider> { IosCaptureMetadataProvider() }
    // Vêm do Info.plist (preenchido no build, ver ios/project.yml); vazio = contas desligadas.
    single { SupabaseConfig(url = infoString("SupabaseUrl"), anonKey = infoString("SupabaseAnonKey")) }
    single { PlatformCredentialsCleaner.None }
    single<ImageLoader> { IosImageLoader() }
    single { AppConfig() }
}

private fun infoString(key: String): String = NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? String ?: ""

private var koinStarted = false

/** Chamado pelo MainViewController; o app Swift pode recriar a tela, o Koin sobe uma vez. */
fun initKoin() {
    if (koinStarted) return
    koinStarted = true
    startKoin { modules(sharedModule, presentationModule, iosPlatformModule) }
}
