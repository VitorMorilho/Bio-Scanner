package com.bioscanner.presentation.result

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioscanner.domain.model.ClassificationResult
import com.bioscanner.domain.usecase.ClassifyAndLookupSpeciesUseCase
import com.bioscanner.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val MAX_DECODED_DIMENSION = 1024

@HiltViewModel
class ResultViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val classifyAndLookupSpecies: ClassifyAndLookupSpeciesUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val imageUri: Uri = Uri.parse(
        checkNotNull(savedStateHandle.get<String>(Routes.RESULT_ARG_IMAGE_URI)) {
            "ResultScreen requer o argumento de navegação ${Routes.RESULT_ARG_IMAGE_URI}"
        },
    )

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading(imageUri))
    val uiState: StateFlow<ResultUiState> = _uiState

    init {
        classify()
    }

    private fun classify() {
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) { decodeDownsampledBitmap(context, imageUri) }
            if (bitmap == null) {
                _uiState.value = ResultUiState.Error(imageUri, "Não foi possível carregar a imagem selecionada.")
                return@launch
            }

            when (val result = classifyAndLookupSpecies(bitmap)) {
                is ClassificationResult.Success ->
                    _uiState.value = ResultUiState.Success(imageUri, result.species, result.confidence)
                is ClassificationResult.LowConfidence ->
                    _uiState.value = ResultUiState.LowConfidence(imageUri, result.candidates)
                is ClassificationResult.NoMatch ->
                    _uiState.value = ResultUiState.NoMatch(imageUri)
                is ClassificationResult.Error ->
                    _uiState.value = ResultUiState.Error(imageUri, result.message)
            }
        }
    }

    private fun decodeDownsampledBitmap(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > MAX_DECODED_DIMENSION ||
            bounds.outHeight / sampleSize > MAX_DECODED_DIMENSION
        ) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOptions) }
    }
}
