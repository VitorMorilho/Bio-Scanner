package com.bioscanner.presentation.register

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.observation.CaptureMetadataProvider
import com.bioscanner.domain.repository.RegionRepository
import com.bioscanner.domain.usecase.RegisterObservationUseCase
import com.bioscanner.presentation.common.regionNameAt
import com.bioscanner.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "RegisterViewModel"

data class RegisterUiState(
    val photoUri: String,
    val loadingMetadata: Boolean = true,
    val metadata: CaptureMetadata? = null,
    val regionName: String? = null,
    val sending: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val metadataProvider: CaptureMetadataProvider,
    private val regionRepository: RegionRepository,
    private val registerObservation: RegisterObservationUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // O Navigation já devolve o argumento decodificado (foi codificado com Uri.encode na ida).
    private val photoUri: String = checkNotNull(savedStateHandle[Routes.REGISTER_ARG_PHOTO_URI])
    private val fromCamera: Boolean = checkNotNull(savedStateHandle.get<String>(Routes.REGISTER_ARG_FROM_CAMERA)).toBoolean()

    private val _uiState = MutableStateFlow(RegisterUiState(photoUri = photoUri))
    val uiState: StateFlow<RegisterUiState> = _uiState

    private val _saved = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val saved: SharedFlow<Long> = _saved

    init {
        loadMetadata()
    }

    /** Chamado quando o usuário responde ao pedido de localização. */
    fun onLocationPermissionResult(granted: Boolean) {
        if (granted && _uiState.value.metadata?.location == null) loadMetadata()
    }

    private fun loadMetadata() {
        _uiState.update { it.copy(loadingMetadata = true) }
        viewModelScope.launch {
            val fresh = if (fromCamera) {
                metadataProvider.forCameraPhoto()
            } else {
                metadataProvider.forGalleryPhoto(photoUri)
            }
            // Ao recarregar só por causa da localização, a hora da 1ª leitura
            // (momento em que a foto da câmera foi tirada) é mantida.
            val metadata = _uiState.value.metadata
                ?.copy(location = fresh.location, locationOrigin = fresh.locationOrigin)
                ?: fresh
            val regionName = metadata.location?.let { regionRepository.getRegionCatalog().regions.regionNameAt(it) }
            _uiState.update { it.copy(loadingMetadata = false, metadata = metadata, regionName = regionName) }
        }
    }

    fun send() {
        val metadata = _uiState.value.metadata ?: return
        _uiState.update { it.copy(sending = true, error = null) }
        viewModelScope.launch {
            runCatching { registerObservation(photoUri, metadata) }
                .onSuccess { _saved.tryEmit(it) }
                .onFailure { error ->
                    Log.e(TAG, "Falha ao salvar o registro", error)
                    _uiState.update { s -> s.copy(sending = false, error = "Não foi possível salvar o registro. Tente novamente.") }
                }
        }
    }
}
