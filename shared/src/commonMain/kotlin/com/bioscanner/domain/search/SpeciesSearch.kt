package com.bioscanner.domain.search

import com.bioscanner.domain.model.Species

/**
 * Busca sem acento e sem maiúsculas em nome popular, científico, nomes
 * alternativos e família — "jararaca" acha "Jararacuçu", "boipeva" acha
 * Xenodon merremii pelo nome alternativo.
 */
fun Species.matches(query: String): Boolean {
    val q = query.normalizedForSearch()
    if (q.isEmpty()) return true
    return (listOfNotNull(popularName, scientificName, family) + otherNames)
        .any { it.normalizedForSearch().contains(q) }
}

// Sem java.text.Normalizer no iOS: tabela dos acentos usados em português
// (os nomes vêm do GBIF/CSV em pt-BR e latim, que não tem acento).
private val ACCENTS = mapOf(
    'á' to 'a', 'à' to 'a', 'â' to 'a', 'ã' to 'a', 'ä' to 'a',
    'é' to 'e', 'è' to 'e', 'ê' to 'e', 'ë' to 'e',
    'í' to 'i', 'ì' to 'i', 'î' to 'i', 'ï' to 'i',
    'ó' to 'o', 'ò' to 'o', 'ô' to 'o', 'õ' to 'o', 'ö' to 'o',
    'ú' to 'u', 'ù' to 'u', 'û' to 'u', 'ü' to 'u',
    'ç' to 'c', 'ñ' to 'n',
)

internal fun String.normalizedForSearch(): String =
    trim().lowercase().map { ACCENTS[it] ?: it }.joinToString("")
