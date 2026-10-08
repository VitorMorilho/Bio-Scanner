package com.bioscanner.presentation.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

// ~24 MB: cabe a lista inteira de um estado (600 px cada) sem redecodificar ao rolar.
private val cache = object : LruCache<String, ImageBitmap>(24 * 1024 * 1024) {
    override fun sizeOf(key: String, value: ImageBitmap) = value.width * value.height * 4
}

/**
 * Foto de espécie empacotada em assets/ (gerada por tools/data/build_species_data.py).
 * Sem biblioteca de imagens: são arquivos locais e pequenos, basta decodificar
 * fora da main thread e guardar num LruCache. Sem foto, mostra um ícone neutro.
 */
@Composable
fun SpeciesImage(
    assetPath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    CachedImage(
        key = assetPath?.let { "asset:$it" },
        decode = { decodeSampled(maxSizePx = Int.MAX_VALUE) { context.assets.open(assetPath!!) } },
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

/**
 * Foto de um registro: caminho de arquivo (cópia salva em filesDir) ou Uri
 * content:// (foto recém-tirada/escolhida, antes de salvar). [maxSizePx] reduz
 * na decodificação — miniaturas não precisam da foto inteira.
 */
@Composable
fun ObservationPhoto(
    photo: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxSizePx: Int = 1600,
) {
    val context = LocalContext.current
    CachedImage(
        key = "photo:$photo@$maxSizePx",
        decode = {
            decodeSampled(maxSizePx) {
                if (photo.startsWith("content:") || photo.startsWith("file:")) {
                    checkNotNull(context.contentResolver.openInputStream(Uri.parse(photo)))
                } else {
                    File(photo).inputStream()
                }
            }
        },
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

@Composable
private fun CachedImage(
    key: String?,
    decode: () -> Bitmap?,
    contentDescription: String?,
    modifier: Modifier,
) {
    val bitmap by produceState(initialValue = key?.let { cache.get(it) }, key) {
        if (key != null && value == null) {
            value = withContext(Dispatchers.IO) {
                runCatching(decode).getOrNull()?.asImageBitmap()?.also { cache.put(key, it) }
            }
        }
    }

    val current = bitmap
    if (current != null) {
        Image(
            bitmap = current,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Pets,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
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
