package com.bioscanner.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioscanner.domain.model.Species
import com.bioscanner.domain.repository.RegionRepository
import com.bioscanner.domain.usecase.GetSpeciesByIdUseCase
import com.bioscanner.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SpeciesDetailViewModel @Inject constructor(
    getSpeciesById: GetSpeciesByIdUseCase,
    regionRepository: RegionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val speciesId: String = checkNotNull(savedStateHandle[Routes.DETAIL_ARG_SPECIES_ID])

    val species: StateFlow<Species?> = getSpeciesById(speciesId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Nomes dos estados com registro, do mais para o menos registrado. */
    val regionNames: StateFlow<List<String>> = regionRepository.observeRegionIdsForSpecies(speciesId)
        .map { ids ->
            val names = regionRepository.getRegionCatalog().regions.associate { it.id to it.name }
            ids.mapNotNull { names[it] }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
