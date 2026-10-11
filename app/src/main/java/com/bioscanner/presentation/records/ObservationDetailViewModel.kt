package com.bioscanner.presentation.records

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioscanner.domain.model.Observation
import com.bioscanner.domain.repository.ObservationRepository
import com.bioscanner.domain.repository.RegionRepository
import com.bioscanner.domain.usecase.DeleteObservationUseCase
import com.bioscanner.domain.model.regionNameAt
import com.bioscanner.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ObservationDetailUiState(
    val loading: Boolean = true,
    val observation: Observation? = null,
    val regionName: String? = null,
)

@HiltViewModel
class ObservationDetailViewModel @Inject constructor(
    observationRepository: ObservationRepository,
    regionRepository: RegionRepository,
    private val deleteObservation: DeleteObservationUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _deleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val deleted: SharedFlow<Unit> = _deleted

    private val observationId: Long = checkNotNull(savedStateHandle.get<String>(Routes.OBSERVATION_ARG_ID)).toLong()

    // Flow do Room: emite null quando o registro é excluído.
    val uiState: StateFlow<ObservationDetailUiState> = observationRepository.observeById(observationId)
        .map { observation ->
            val regionName = observation?.metadata?.location?.let {
                regionRepository.getRegionCatalog().regions.regionNameAt(it)
            }
            ObservationDetailUiState(loading = false, observation = observation, regionName = regionName)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ObservationDetailUiState())

    fun delete() {
        viewModelScope.launch {
            deleteObservation(observationId)
            _deleted.tryEmit(Unit)
        }
    }
}
