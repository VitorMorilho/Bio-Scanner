package com.bioscanner.data.observation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

private const val PHOTOS_DIR = "observations"
private const val MAX_DIMENSION = 1600
private const val JPEG_QUALITY = 85

/**
 * Copia a foto para filesDir/observations/. A Uri do seletor de fotos só vale
 * enquanto o app está aberto, então o registro não pode depender dela.
 *
 * Na cópia a foto já é reduzida (≤1600 px) e girada conforme o EXIF: fotos da
 * câmera vêm "deitadas" com uma tag de orientação que o BitmapFactory ignora.
 * Os metadados de local/hora são lidos ANTES, da original (CaptureMetadataProvider).
 */
class InternalPhotoStorage(
    private val context: Context,
) : com.bioscanner.domain.observation.PhotoStorage {

    override suspend fun import(photoUri: String): String = withContext(Dispatchers.IO) {
        val uri = Uri.parse(photoUri)
        val resolver = context.contentResolver

        // Com inJustDecodeBounds o decodeStream SEMPRE retorna null (só preenche
        // bounds), então o "não abriu" é checado no stream, não no retorno.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val stream = resolver.openInputStream(uri) ?: throw IOException("Não foi possível abrir a foto: $photoUri")
        stream.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("Formato de imagem não suportado: $photoUri")
        }

        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MAX_DIMENSION) sampleSize *= 2
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sampleSize })
        } ?: throw IOException("Não foi possível decodificar a foto: $photoUri")

        val rotation = resolver.openInputStream(uri)?.use { exifRotation(ExifInterface(it)) } ?: 0
        val bitmap = scaleAndRotate(decoded, rotation)

        val dir = File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        file.absolutePath
    }

    override suspend fun delete(photoPath: String) {
        withContext(Dispatchers.IO) {
            // Só apaga o que está na pasta de registros do app, nunca uma foto do usuário.
            val file = File(photoPath)
            if (file.parentFile?.canonicalPath == File(context.filesDir, PHOTOS_DIR).canonicalPath) {
                file.delete()
            }
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            File(context.filesDir, PHOTOS_DIR).deleteRecursively()
        }
    }

    override suspend fun readBytes(photoPath: String): ByteArray? = withContext(Dispatchers.IO) {
        File(photoPath).takeIf { it.isFile }?.readBytes()
    }

    override suspend fun save(bytes: ByteArray): String = withContext(Dispatchers.IO) {
        // Vem da nuvem já reduzida e rotacionada (foi gravada por import), então só copia.
        val dir = File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.writeBytes(bytes)
        file.absolutePath
    }

    private fun exifRotation(exif: ExifInterface): Int =
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

    private fun scaleAndRotate(bitmap: Bitmap, rotation: Int): Bitmap {
        val scale = minOf(1f, MAX_DIMENSION.toFloat() / maxOf(bitmap.width, bitmap.height))
        if (scale == 1f && rotation == 0) return bitmap
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postRotate(rotation.toFloat())
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
