package com.bioscanner.domain.model

/**
 * Um candidato retornado pelo classificador de imagem. [label] deve
 * corresponder a [com.bioscanner.data.local.db.SpeciesEntity.classifierLabel],
 * não necessariamente ao [Species.id] — o rótulo do modelo pode divergir do
 * slug interno do banco de dados.
 */
data class ClassificationCandidate(
    val label: String,
    val confidence: Float,
)
