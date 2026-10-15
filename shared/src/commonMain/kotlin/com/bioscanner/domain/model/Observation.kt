package com.bioscanner.domain.model

/** Um registro feito pelo usuário: foto + quando/onde foi tirada. */
data class Observation(
    val id: Long,
    /** Caminho da cópia da foto no armazenamento interno do app. */
    val photoPath: String,
    val metadata: CaptureMetadata,
    val createdAt: Long,
)

data class GeoPoint(val latitude: Double, val longitude: Double)

/** De onde veio cada dado do registro, para a UI deixar claro quando é estimado. */
enum class MetadataOrigin {
    /** Lido dos metadados (EXIF) da própria foto. */
    PHOTO,

    /** Do aparelho no momento do registro (foto da câmera, ou galeria sem EXIF). */
    DEVICE,
}

data class CaptureMetadata(
    val takenAt: Long,
    val takenAtOrigin: MetadataOrigin,
    val location: GeoPoint?,
    val locationOrigin: MetadataOrigin?,
)
