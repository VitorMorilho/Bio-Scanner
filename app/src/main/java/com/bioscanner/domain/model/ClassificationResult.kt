package com.bioscanner.domain.model

sealed class ClassificationResult {
    data class Success(val species: Species, val confidence: Float) : ClassificationResult()
    data class LowConfidence(val candidates: List<ClassificationCandidate>) : ClassificationResult()
    object NoMatch : ClassificationResult()
    data class Error(val message: String) : ClassificationResult()
}
