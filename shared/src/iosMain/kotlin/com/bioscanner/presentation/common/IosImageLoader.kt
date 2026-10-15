package com.bioscanner.presentation.common

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.bioscanner.data.platform.BUNDLE_ASSETS_DIR
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy

/**
 * Decodifica com o Skia (o mesmo motor que desenha o Compose no iOS). Sem redução na
 * decodificação: as fotos salvas pelo app já são limitadas a 1600 px.
 * TODO(iOS): aplicar a orientação EXIF de fotos ainda não salvas (o PhotoStorage do iOS deve
 * gravar a foto já rotacionada, como o InternalPhotoStorage do Android).
 */
class IosImageLoader : ImageLoader {

    override suspend fun loadBundled(path: String): ImageBitmap? = withContext(Dispatchers.IO) {
        val fullPath = NSBundle.mainBundle.resourcePath?.let { "$it/$BUNDLE_ASSETS_DIR/$path" } ?: return@withContext null
        NSData.dataWithContentsOfFile(fullPath)?.decode()
    }

    override suspend fun loadPhoto(photo: String, maxSizePx: Int): ImageBitmap? = withContext(Dispatchers.IO) {
        val data = if (photo.startsWith("file:")) {
            NSURL.URLWithString(photo)?.let { NSData.dataWithContentsOfURL(it) }
        } else {
            NSData.dataWithContentsOfFile(photo)
        }
        data?.decode()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.decode(): ImageBitmap? {
    val bytes = ByteArray(length.toInt())
    if (bytes.isNotEmpty()) bytes.usePinned { memcpy(it.addressOf(0), this.bytes, length) }
    return runCatching { Image.makeFromEncoded(bytes).toComposeImageBitmap() }.getOrNull()
}
