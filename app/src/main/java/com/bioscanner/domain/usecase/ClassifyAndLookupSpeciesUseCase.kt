package com.bioscanner.domain.usecase

import android.graphics.Bitmap
import com.bioscanner.domain.classifier.ClassificationConfig
import com.bioscanner.domain.classifier.ImageClassifier
import com.bioscanner.domain.model.ClassificationResult
import com.bioscanner.domain.repository.SpeciesRepository
import javax.inject.Inject

class ClassifyAndLookupSpeciesUseCase @Inject constructor(
    private val classifier: ImageClassifier,
    private val repository: SpeciesRepository,
) {
    suspend operator fun invoke(bitmap: Bitmap): ClassificationResult {
        val candidates = classifier.classify(bitmap)
        val best = candidates.firstOrNull() ?: return ClassificationResult.NoMatch

        if (!ClassificationConfig.isConfident(best)) {
            return ClassificationResult.LowConfidence(candidates)
        }

        val species = repository.findByClassifierLabel(best.label)
            ?: return ClassificationResult.NoMatch

        return ClassificationResult.Success(species, best.confidence)
    }
}
