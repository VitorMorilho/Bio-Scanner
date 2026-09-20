package com.bioscanner.data.classifier

import android.graphics.Bitmap
import com.bioscanner.domain.classifier.ImageClassifier
import com.bioscanner.domain.model.ClassificationCandidate
import com.bioscanner.domain.repository.SpeciesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

/**
 * Implementação provisória de [ImageClassifier], usada enquanto o modelo
 * .tflite real ainda não foi treinado/embarcado. Deriva um resultado
 * determinístico a partir de propriedades baratas do bitmap (mesma imagem
 * sempre produz o mesmo resultado) e escolhe sempre entre as espécies que
 * já estão semeadas no Room, para não duplicar dados em dois lugares.
 *
 * Ponto de troca: quando o modelo real existir, troque o binding em
 * [com.bioscanner.di.ClassifierModule] de MockImageClassifier para
 * TfLiteImageClassifier — nenhum outro código muda.
 */
class MockImageClassifier @Inject constructor(
    private val repository: SpeciesRepository,
) : ImageClassifier {

    override suspend fun classify(bitmap: Bitmap): List<ClassificationCandidate> {
        val species = repository.observeAllSpecies().first()
        if (species.isEmpty()) return emptyList()

        val seed = bitmap.width.toLong() * 31 + bitmap.height + bitmap.byteCount
        val random = Random(seed)

        // ~1 em 5 resultados fica abaixo do limiar de confiança de propósito,
        // para exercitar o caminho de "baixa confiança" da UI sem hooks especiais.
        val lowConfidenceOutcome = random.nextInt(5) == 0

        val candidateCount = random.nextInt(1, minOf(3, species.size) + 1)
        val picked = species.shuffled(random).take(candidateCount)

        val topConfidence = if (lowConfidenceOutcome) {
            random.nextFloat() * 0.30f + 0.35f // 0.35–0.65
        } else {
            random.nextFloat() * 0.42f + 0.55f // 0.55–0.97
        }

        return picked.mapIndexed { index, sp ->
            val confidence = if (index == 0) {
                topConfidence
            } else {
                topConfidence * random.nextFloat() * 0.8f
            }
            ClassificationCandidate(
                label = sp.let { it.id },
                confidence = confidence,
            )
        }.sortedByDescending { it.confidence }
    }
}
