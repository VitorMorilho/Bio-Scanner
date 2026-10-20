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

    /** Apaga todas as cópias salvas (exclusão de conta). */
    suspend fun deleteAll()

    /** Conteúdo de uma foto salva, para enviar à nuvem; null se o arquivo não existir mais. */
    suspend fun readBytes(photoPath: String): ByteArray?

    /** Grava uma foto baixada da nuvem (já reduzida e rotacionada) e retorna o caminho. */
    suspend fun save(bytes: ByteArray): String
}

/**
 * Leva os registros da conta logada para a nuvem e traz os que estão lá. Sem conta, nada sai do
 * aparelho.
 */
interface ObservationSyncer {
    /** Pede uma sincronização em segundo plano, sem esperar. Sem conta, não faz nada. */
    fun requestSync()

    /** Sincroniza agora e espera. Lança exceção se falhar (ex.: sem internet). */
    suspend fun syncNow(userId: String)

    /** Tira do aparelho os registros e fotos de [userId] (eles continuam na nuvem). */
    suspend fun clearLocal(userId: String)
}
