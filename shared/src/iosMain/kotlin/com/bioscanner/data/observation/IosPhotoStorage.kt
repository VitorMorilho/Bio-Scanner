package com.bioscanner.data.observation

import com.bioscanner.domain.observation.PhotoStorage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask

private const val PHOTOS_DIR = "observations"

/**
 * Copia a foto para Documents/observations/, como o InternalPhotoStorage do Android faz em
 * filesDir/observations/.
 * TODO(iOS): reduzir para 1600 px e gravar já rotacionada (orientação EXIF), como no Android.
 */
@OptIn(ExperimentalForeignApi::class)
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
}
