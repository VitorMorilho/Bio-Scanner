package com.bioscanner.domain.repository

import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.model.Observation
import kotlinx.coroutines.flow.Flow

/**
 * Registros de quem está usando o app agora: os da conta logada, ou os feitos sem conta quando
 * não há login. Um registro criado com login já nasce da conta e entra na fila de envio.
 */
interface ObservationRepository {
    fun observeAll(): Flow<List<Observation>>
    fun observeById(id: Long): Flow<Observation?>

    /** Quantos registros da conta logada ainda não foram enviados (0 sem login). */
    fun observePendingUploadCount(): Flow<Int>

    suspend fun create(photoPath: String, metadata: CaptureMetadata): Long
    suspend fun findById(id: Long): Observation?

    /** Sem conta, apaga na hora; com conta, esconde e deixa a sincronização apagar na nuvem. */
    suspend fun delete(id: Long)
    suspend fun deleteAll()
}
