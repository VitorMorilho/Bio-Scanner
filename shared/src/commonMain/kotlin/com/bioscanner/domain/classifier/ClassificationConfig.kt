package com.bioscanner.domain.classifier

import com.bioscanner.domain.model.ClassificationCandidate

object ClassificationConfig {
    // Valor inicial para o MVP; ajustar quando houver métricas do modelo real.
    const val MIN_CONFIDENCE_THRESHOLD = 0.65f

    fun isConfident(candidate: ClassificationCandidate): Boolean =
        candidate.confidence >= MIN_CONFIDENCE_THRESHOLD
}
