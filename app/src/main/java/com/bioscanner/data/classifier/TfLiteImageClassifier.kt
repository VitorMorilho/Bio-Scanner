package com.bioscanner.data.classifier

import android.content.Context
import android.graphics.Bitmap
import com.bioscanner.domain.classifier.ImageClassifier
import com.bioscanner.domain.model.ClassificationCandidate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val MODEL_ASSET_PATH = "model/model.tflite"
private const val LABELS_ASSET_PATH = "model/labels.txt"

// Ajustar para bater exatamente com o pré-processamento usado no treino
// (MobileNetV3 ou EfficientNet-Lite): tamanho de entrada e faixa de
// normalização ([0,1] ou [-1,1]) precisam ser idênticos aos do lado Python,
// senão o modelo classifica errado silenciosamente.
private const val MODEL_INPUT_SIZE = 224

/**
 * Implementação real do [ImageClassifier], usando TensorFlow Lite.
 *
 * NÃO está funcional nem vinculada no Hilt (ver [com.bioscanner.di.ClassifierModule])
 * nesta primeira versão, pois ainda não existe um modelo .tflite treinado
 * nem `labels.txt` embarcados no app. Este arquivo documenta o design para
 * quando o modelo real chegar:
 *
 * - `labels.txt`: um rótulo por linha; o índice da linha deve ser exatamente
 *   igual ao índice do tensor de saída do modelo (ou seja, ao mapeamento
 *   class_indices do treinamento) — não reordenar alfabeticamente.
 * - Cada rótulo de `labels.txt` deve corresponder a um
 *   `SpeciesEntity.classifierLabel` no banco local.
 * - Pré-processamento: redimensionar o bitmap para [MODEL_INPUT_SIZE] x
 *   [MODEL_INPUT_SIZE] e converter para ByteBuffer com a mesma normalização
 *   usada no treino (tensorflow-lite-support não é usado aqui devido a um
 *   bug de empacotamento incompatível com AGP 9 — ver comentário em
 *   app/build.gradle.kts).
 * - Rodar `Interpreter.run(input, output)` e converter as probabilidades de
 *   saída em [ClassificationCandidate]s ordenados por confiança
 *   decrescente (top-K, ex.: top-3).
 *
 * Ponto de troca: uma vez implementado, trocar o binding em
 * [com.bioscanner.di.ClassifierModule] de MockImageClassifier para esta
 * classe — nenhum outro código de domain/presentation precisa mudar.
 */
class TfLiteImageClassifier @Inject constructor(
    @ApplicationContext private val context: Context,
) : ImageClassifier {

    override suspend fun classify(bitmap: Bitmap): List<ClassificationCandidate> {
        throw NotImplementedError(
            "TfLiteImageClassifier ainda não está implementado: nenhum modelo .tflite " +
                "foi embarcado ainda. Use MockImageClassifier (binding padrão em " +
                "ClassifierModule) até o modelo real e o dataset estarem disponíveis."
        )
    }
}
