package com.bioscanner.data.sync

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Ver supabase/migrations/20261020000000_observations.sql.
private const val TABLE = "observations"
private const val BUCKET = "observations"

@Serializable
private data class ObservationRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("taken_at") val takenAt: Long,
    @SerialName("taken_at_origin") val takenAtOrigin: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("location_origin") val locationOrigin: String? = null,
    @SerialName("created_at") val createdAt: Long,
)

class SupabaseObservationRemote(private val supabase: SupabaseClient) : ObservationRemote {

    private val table get() = supabase.from(TABLE)
    private val bucket get() = supabase.storage.from(BUCKET)

    override suspend fun list(userId: String): List<RemoteObservation> =
        // O RLS já limita ao próprio usuário; o filtro deixa isso explícito.
        table.select { filter { eq("user_id", userId) } }
            .decodeList<ObservationRow>()
            .map { it.toRemote() }

    override suspend fun upload(userId: String, observation: RemoteObservation, photo: ByteArray) {
        // Foto antes da linha: a linha nunca aponta para uma foto que não subiu.
        bucket.upload(photoPath(userId, observation.id), photo) {
            upsert = true
            contentType = ContentType.Image.JPEG
        }
        table.upsert(observation.toRow(userId)) {
            onConflict = "id"
            ignoreDuplicates = true
        }
    }

    override suspend fun downloadPhoto(userId: String, id: String): ByteArray =
        bucket.downloadAuthenticated(photoPath(userId, id))

    override suspend fun delete(userId: String, id: String) {
        // Linha antes da foto: se a foto falhar, sobra só um arquivo que ninguém vê.
        table.delete { filter { eq("id", id) } }
        bucket.delete(listOf(photoPath(userId, id)))
    }

    private fun photoPath(userId: String, id: String) = "$userId/$id.jpg"
}

private fun ObservationRow.toRemote() = RemoteObservation(
    id = id,
    takenAt = takenAt,
    takenAtOrigin = takenAtOrigin,
    latitude = latitude,
    longitude = longitude,
    locationOrigin = locationOrigin,
    createdAt = createdAt,
)

private fun RemoteObservation.toRow(userId: String) = ObservationRow(
    id = id,
    userId = userId,
    takenAt = takenAt,
    takenAtOrigin = takenAtOrigin,
    latitude = latitude,
    longitude = longitude,
    locationOrigin = locationOrigin,
    createdAt = createdAt,
)
