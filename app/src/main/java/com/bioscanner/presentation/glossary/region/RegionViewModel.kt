package com.bioscanner.presentation.glossary.region

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioscanner.domain.model.Region
import com.bioscanner.domain.model.RegionCatalog
import com.bioscanner.domain.model.RegionalSpecies
import com.bioscanner.domain.repository.RegionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_SELECTED_REGION = "selectedRegionId"
private const val KEY_VENOMOUS_ONLY = "venomousOnly"

data class RegionUiState(
    val loading: Boolean = true,
    val regions: List<Region> = emptyList(),
    val selectedRegion: Region? = null,
    /** Total de espécies da região, antes do filtro "só peçonhentas". */
    val totalSpeciesInRegion: Int = 0,
    val species: List<RegionalSpecies> = emptyList(),
    val venomousOnly: Boolean = false,
    val source: String = "",
    val generatedAt: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RegionViewModel @Inject constructor(
    repository: RegionRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val catalog = MutableStateFlow<RegionCatalog?>(null)
    private val selectedId = savedStateHandle.getStateFlow<String?>(KEY_SELECTED_REGION, null)

    private val speciesInSelected = selectedId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.observeSpeciesInRegion(id)
    }

    val uiState: StateFlow<RegionUiState> = combine(
        catalog,
        selectedId,
        speciesInSelected,
        savedStateHandle.getStateFlow(KEY_VENOMOUS_ONLY, false),
    ) { loaded, id, all, venomousOnly ->
        if (loaded == null) return@combine RegionUiState()
        RegionUiState(
            loading = false,
            regions = loaded.regions,
            selectedRegion = loaded.regions.firstOrNull { it.id == id },
            totalSpeciesInRegion = all.size,
            species = if (venomousOnly) all.filter { it.species.venomous } else all,
            venomousOnly = venomousOnly,
            source = loaded.source,
            generatedAt = loaded.generatedAt,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RegionUiState())

    init {
        viewModelScope.launch { catalog.value = repository.getRegionCatalog() }
    }

    fun selectRegion(regionId: String) {
        savedStateHandle[KEY_SELECTED_REGION] = regionId
    }

    fun setVenomousOnly(enabled: Boolean) {
        savedStateHandle[KEY_VENOMOUS_ONLY] = enabled
    }
}
