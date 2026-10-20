package com.bioscanner.data.sync

/** Um registro como fica na nuvem; a foto é guardada à parte, pelo [id]. */
data class RemoteObservation(
    val id: String,
    val takenAt: Long,
    val takenAtOrigin: String,
    val latitude: Double?,
    val longitude: Double?,
    val locationOrigin: String?,
    val createdAt: Long,
)

/** Registros de um usuário na nuvem. Todas as funções lançam exceção se a rede falhar. */
interface ObservationRemote {
    suspend fun list(userId: String): List<RemoteObservation>

    /** Idempotente: reenviar um registro que já subiu não duplica nem falha. */
    suspend fun upload(userId: String, observation: RemoteObservation, photo: ByteArray)

    suspend fun downloadPhoto(userId: String, id: String): ByteArray

    /** Não falha se o registro já não existir. */
    suspend fun delete(userId: String, id: String)
}
