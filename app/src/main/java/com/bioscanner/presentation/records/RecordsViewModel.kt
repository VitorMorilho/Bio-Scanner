package com.bioscanner.presentation.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioscanner.domain.model.Observation
import com.bioscanner.domain.repository.ObservationRepository
import com.bioscanner.domain.repository.RegionRepository
import com.bioscanner.presentation.common.regionNameAt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Usuário fixo enquanto não há login. Quando houver conta, vem de um
 * repositório de usuário — a tela só lê estes campos.
 */
data class UserProfile(val name: String, val subtitle: String)

private val MOCK_USER = UserProfile(
    name = "Usuário de demonstração",
    subtitle = "Login em breve. Seus registros ficam salvos neste aparelho.",
)

data class RecordItem(val observation: Observation, val regionName: String?)

data class RecordsUiState(
    val loading: Boolean = true,
    val user: UserProfile = MOCK_USER,
    val records: List<RecordItem> = emptyList(),
) {
    val withLocationCount: Int get() = records.count { it.observation.metadata.location != null }
    val distinctRegionsCount: Int get() = records.mapNotNull { it.regionName }.distinct().size
}

@HiltViewModel
class RecordsViewModel @Inject constructor(
    observationRepository: ObservationRepository,
    regionRepository: RegionRepository,
) : ViewModel() {

    val uiState: StateFlow<RecordsUiState> = observationRepository.observeAll()
        .map { observations ->
            val regions = regionRepository.getRegionCatalog().regions
            RecordsUiState(
                loading = false,
                records = observations.map { o ->
                    RecordItem(o, o.metadata.location?.let { regions.regionNameAt(it) })
                },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordsUiState())
}
