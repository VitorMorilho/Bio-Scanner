package com.bioscanner.presentation.glossary.species

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioscanner.domain.model.ColorPattern
import com.bioscanner.domain.model.Species
import com.bioscanner.domain.repository.SpeciesRepository
import com.bioscanner.domain.search.hasAnyPattern
import com.bioscanner.domain.search.matches
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val KEY_QUERY = "query"

// Guardado como texto ("GREEN,LINED"): String é salvável em qualquer plataforma.
private const val KEY_PATTERNS = "patterns"

data class SpeciesSearchUiState(
    val loading: Boolean = true,
    val query: String = "",
    val selectedPatterns: Set<ColorPattern> = emptySet(),
    val results: List<Species> = emptyList(),
    val total: Int = 0,
)

class SpeciesSearchViewModel(
    repository: SpeciesRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val uiState: StateFlow<SpeciesSearchUiState> = combine(
        repository.observeAllSpecies(),
        savedStateHandle.getStateFlow(KEY_QUERY, ""),
        savedStateHandle.getStateFlow(KEY_PATTERNS, ""),
    ) { all, query, patternNames ->
        val patterns = patternNames.toPatterns()
        SpeciesSearchUiState(
            loading = false,
            query = query,
            selectedPatterns = patterns,
            results = all.filter { it.matches(query) && it.hasAnyPattern(patterns) },
            total = all.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpeciesSearchUiState())

    fun onQueryChange(query: String) {
        savedStateHandle[KEY_QUERY] = query
    }

    fun onPatternToggle(pattern: ColorPattern) {
        val current = savedStateHandle.get<String>(KEY_PATTERNS).orEmpty().toPatterns()
        val updated = if (pattern in current) current - pattern else current + pattern
        savedStateHandle[KEY_PATTERNS] = updated.joinToString(",") { it.name }
    }
}

private fun String.toPatterns(): Set<ColorPattern> =
    split(",").mapNotNull { name -> ColorPattern.entries.find { it.name == name } }.toSet()
