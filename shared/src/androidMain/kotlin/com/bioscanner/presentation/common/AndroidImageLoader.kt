package com.bioscanner.presentation.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class AndroidImageLoader(private val context: Context) : ImageLoader {

    override suspend fun loadBundled(path: String): ImageBitmap? = withContext(Dispatchers.IO) {
        decodeSampled(maxSizePx = Int.MAX_VALUE) { context.assets.open(path) }?.asImageBitmap()
    }

    override suspend fun loadPhoto(photo: String, maxSizePx: Int): ImageBitmap? = withContext(Dispatchers.IO) {
        decodeSampled(maxSizePx) {
            if (photo.startsWith("content:") || photo.startsWith("file:")) {
                checkNotNull(context.contentResolver.openInputStream(Uri.parse(photo)))
            } else {
                File(photo).inputStream()
            }
        }?.asImageBitmap()
    }
}

private fun decodeSampled(maxSizePx: Int, open: () -> InputStream): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    open().use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSizePx) sample *= 2
    val bitmap = open().use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
    } ?: return null
    // Fotos da câmera vêm com a orientação só na tag EXIF, que o BitmapFactory ignora.
    val degrees = runCatching {
        open().use {
            when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }
    }.getOrDefault(0f)
    if (degrees == 0f) return bitmap
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(degrees) }, true)
}
