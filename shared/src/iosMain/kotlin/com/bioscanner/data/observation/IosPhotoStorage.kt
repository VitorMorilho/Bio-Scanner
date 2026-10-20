package com.bioscanner.data.observation

import com.bioscanner.domain.observation.PhotoStorage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToURL
import platform.posix.memcpy

private const val PHOTOS_DIR = "observations"

/**
 * Copia a foto para Documents/observations/, como o InternalPhotoStorage do Android faz em
 * filesDir/observations/.
 * TODO(iOS): reduzir para 1600 px e gravar já rotacionada (orientação EXIF), como no Android.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosPhotoStorage : PhotoStorage {

    private val fileManager get() = NSFileManager.defaultManager

    private fun photosDir(): NSURL {
        val documents = requireNotNull(
            fileManager.URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, true, null),
        ) { "Pasta Documents indisponível" }
        val dir = requireNotNull(documents.URLByAppendingPathComponent(PHOTOS_DIR))
        fileManager.createDirectoryAtURL(dir, withIntermediateDirectories = true, attributes = null, error = null)
        return dir
    }

    override suspend fun import(photoUri: String): String = withContext(Dispatchers.IO) {
        val source = if (photoUri.startsWith("file:")) NSURL.URLWithString(photoUri) else NSURL.fileURLWithPath(photoUri)
        val target = requireNotNull(photosDir().URLByAppendingPathComponent("${NSUUID().UUIDString}.jpg"))
        check(fileManager.copyItemAtURL(requireNotNull(source), target, null)) { "Falha ao copiar a foto" }
        requireNotNull(target.path)
    }

    override suspend fun delete(photoPath: String) {
        withContext(Dispatchers.IO) { fileManager.removeItemAtPath(photoPath, null) }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) { fileManager.removeItemAtURL(photosDir(), null) }
    }

    override suspend fun readBytes(photoPath: String): ByteArray? = withContext(Dispatchers.IO) {
        NSData.dataWithContentsOfFile(photoPath)?.toByteArray()
    }

    override suspend fun save(bytes: ByteArray): String = withContext(Dispatchers.IO) {
        // Vem da nuvem já pronta (foi gravada por import), então só grava o arquivo.
        val target = requireNotNull(photosDir().URLByAppendingPathComponent("${NSUUID().UUIDString}.jpg"))
        check(bytes.toNSData().writeToURL(target, atomically = true)) { "Falha ao gravar a foto" }
        requireNotNull(target.path)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val result = ByteArray(size)
    if (size > 0) result.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return result
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { NSData.create(bytes = it.addressOf(0), length = size.toULong()) }
}
