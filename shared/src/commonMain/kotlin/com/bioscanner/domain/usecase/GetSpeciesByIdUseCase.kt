package com.bioscanner.domain.usecase

import com.bioscanner.domain.model.Species
import com.bioscanner.domain.repository.SpeciesRepository
import kotlinx.coroutines.flow.Flow

class GetSpeciesByIdUseCase(
    private val repository: SpeciesRepository,
) {
    operator fun invoke(id: String): Flow<Species?> = repository.observeSpeciesById(id)
}
