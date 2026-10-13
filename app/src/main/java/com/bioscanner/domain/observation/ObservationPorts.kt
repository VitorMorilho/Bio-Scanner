package com.bioscanner.domain.observation

import com.bioscanner.domain.model.CaptureMetadata

/** Quando e onde a foto foi tirada. */
interface CaptureMetadataProvider {
    /** Foto tirada agora pela câmera do app: hora e posição atuais. */
    suspend fun forCameraPhoto(): CaptureMetadata

    /** Foto da galeria: tenta o EXIF da foto e cai para hora/posição atuais. */
    suspend fun forGalleryPhoto(photoUri: String): CaptureMetadata
}

/** Guarda uma cópia da foto no armazenamento do app (a Uri original pode expirar). */
interface PhotoStorage {
    /** Retorna o caminho da cópia salva. */
    suspend fun import(photoUri: String): String

    /** Apaga a cópia salva por [import]. Não falha se o arquivo já não existir. */
    suspend fun delete(photoPath: String)
}
