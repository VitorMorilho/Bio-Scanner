package com.bioscanner.domain.classifier

import android.graphics.Bitmap
import com.bioscanner.domain.model.ClassificationCandidate

/**
 * Abstração genérica de "um modelo de classificação de imagem" — não é
 * específica de cobras, mesmo que o único modelo embarcado hoje só
 * reconheça cobras. Implementações devem retornar candidatos já ordenados
 * por confiança decrescente.
 */
interface ImageClassifier {
    suspend fun classify(bitmap: Bitmap): List<ClassificationCandidate>
}
