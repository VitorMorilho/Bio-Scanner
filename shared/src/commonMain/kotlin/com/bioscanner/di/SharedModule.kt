package com.bioscanner.di

import androidx.room.RoomDatabase
import com.bioscanner.data.auth.SupabaseAuthRepository
import com.bioscanner.data.auth.createBioScannerSupabaseClient
import com.bioscanner.data.local.db.BioScannerDatabase
import com.bioscanner.data.local.db.SeedingCallback
import com.bioscanner.data.local.db.configure
import com.bioscanner.data.local.seed.SpeciesSeedLoader
import com.bioscanner.data.repository.AssetRegionRepository
import com.bioscanner.data.repository.ObservationRepositoryImpl
import com.bioscanner.data.repository.SpeciesRepositoryImpl
import com.bioscanner.domain.auth.AuthRepository
import com.bioscanner.domain.repository.ObservationRepository
import com.bioscanner.domain.repository.RegionRepository
import com.bioscanner.domain.repository.SpeciesRepository
import com.bioscanner.domain.usecase.DeleteAccountUseCase
import com.bioscanner.domain.usecase.DeleteObservationUseCase
import com.bioscanner.domain.usecase.GetSpeciesByIdUseCase
import com.bioscanner.domain.usecase.RegisterObservationUseCase
import com.bioscanner.presentation.auth.AuthViewModel
import com.bioscanner.presentation.capture.CaptureViewModel
import com.bioscanner.presentation.detail.SpeciesDetailViewModel
import com.bioscanner.presentation.glossary.region.RegionViewModel
import com.bioscanner.presentation.glossary.species.SpeciesSearchViewModel
import com.bioscanner.presentation.records.ObservationDetailViewModel
import com.bioscanner.presentation.records.RecordsViewModel
import com.bioscanner.presentation.register.RegisterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Dependências comuns a Android e iOS. Cada plataforma soma um módulo que fornece:
 * `RoomDatabase.Builder<BioScannerDatabase>`, `ResourceReader`, `SeedVersionStore`,
 * `PhotoStorage`, `CaptureMetadataProvider`, `SupabaseConfig`, `PlatformCredentialsCleaner`,
 * `ImageLoader` e `AppConfig`.
 */
@OptIn(ExperimentalTime::class)
val sharedModule = module {
    // Escopo da aplicação: o seed do banco roda nele e não pode ser cancelado com uma tela.
    single { CoroutineScope(SupervisorJob()) }

    single<BioScannerDatabase> {
        get<RoomDatabase.Builder<BioScannerDatabase>>().configure(
            SeedingCallback(
                seedLoader = SpeciesSeedLoader(get()),
                versionStore = get(),
                scope = get(),
                // Preguiçoso: o callback só usa o banco ao abrir, quando o singleton já existe.
                database = { get() },
            ),
        )
    }
    single { get<BioScannerDatabase>().speciesDao() }
    single { get<BioScannerDatabase>().observationDao() }

    single<SpeciesRepository> { SpeciesRepositoryImpl(get()) }
    single<RegionRepository> { AssetRegionRepository(get(), get()) }
    single<ObservationRepository> { ObservationRepositoryImpl(get(), now = { Clock.System.now().toEpochMilliseconds() }) }

    single { createBioScannerSupabaseClient(get()) }
    single<AuthRepository> { SupabaseAuthRepository(get(), get(), get()) }

    factoryOf(::GetSpeciesByIdUseCase)
    factoryOf(::RegisterObservationUseCase)
    factoryOf(::DeleteObservationUseCase)
    factoryOf(::DeleteAccountUseCase)
}

/** ViewModels das telas comuns (o SavedStateHandle traz os argumentos da navegação). */
val presentationModule = module {
    viewModelOf(::AuthViewModel)
    viewModelOf(::CaptureViewModel)
    viewModelOf(::SpeciesDetailViewModel)
    viewModelOf(::RegionViewModel)
    viewModelOf(::SpeciesSearchViewModel)
    viewModelOf(::ObservationDetailViewModel)
    viewModelOf(::RecordsViewModel)
    viewModelOf(::RegisterViewModel)
}
