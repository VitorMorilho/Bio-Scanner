package com.bioscanner.presentation.result

import android.net.Uri
import com.bioscanner.domain.model.ClassificationCandidate
import com.bioscanner.domain.model.Species

sealed class ResultUiState {
    data class Loading(val imageUri: Uri) : ResultUiState()
    data class Success(val imageUri: Uri, val species: Species, val confidence: Float) : ResultUiState()
    data class LowConfidence(val imageUri: Uri, val candidates: List<ClassificationCandidate>) : ResultUiState()
    data class NoMatch(val imageUri: Uri) : ResultUiState()
    data class Error(val imageUri: Uri, val message: String) : ResultUiState()
}
