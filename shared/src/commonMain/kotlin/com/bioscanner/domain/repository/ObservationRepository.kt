package com.bioscanner.domain.repository

import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.model.Observation
import kotlinx.coroutines.flow.Flow

interface ObservationRepository {
    fun observeAll(): Flow<List<Observation>>
    fun observeById(id: Long): Flow<Observation?>
    suspend fun create(photoPath: String, metadata: CaptureMetadata): Long
    suspend fun findById(id: Long): Observation?
    suspend fun delete(id: Long)
}
