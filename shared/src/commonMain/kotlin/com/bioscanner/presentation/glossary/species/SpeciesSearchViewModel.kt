package com.bioscanner.presentation.glossary.species

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioscanner.domain.model.Species
import com.bioscanner.domain.repository.SpeciesRepository
import com.bioscanner.domain.search.matches
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val KEY_QUERY = "query"

data class SpeciesSearchUiState(
    val loading: Boolean = true,
    val query: String = "",
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
    ) { all, query ->
        SpeciesSearchUiState(
            loading = false,
            query = query,
            results = all.filter { it.matches(query) },
            total = all.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpeciesSearchUiState())

    fun onQueryChange(query: String) {
        savedStateHandle[KEY_QUERY] = query
    }
}
