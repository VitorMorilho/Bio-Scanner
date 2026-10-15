package com.bioscanner.presentation.common

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
import androidx.compose.ui.layout.ContentScale
import org.koin.compose.koinInject

// ~24 MB: cabe a lista inteira de um estado (600 px cada) sem redecodificar ao rolar.
private val cache = ImageCache(24L * 1024 * 1024)

/**
 * Foto de espécie empacotada com o app (gerada por tools/data/build_species_data.py).
 * Sem biblioteca de imagens: são arquivos locais e pequenos, basta decodificar
 * fora da main thread e guardar num cache. Sem foto, mostra um ícone neutro.
 */
@Composable
fun SpeciesImage(
    assetPath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val loader = koinInject<ImageLoader>()
    CachedImage(
        key = assetPath?.let { "asset:$it" },
        decode = { loader.loadBundled(assetPath!!) },
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

/**
 * Foto de um registro: caminho de arquivo (cópia salva pelo app) ou URI da
 * plataforma (foto recém-tirada/escolhida, antes de salvar). [maxSizePx] reduz
 * na decodificação — miniaturas não precisam da foto inteira.
 */
@Composable
fun ObservationPhoto(
    photo: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxSizePx: Int = 1600,
) {
    val loader = koinInject<ImageLoader>()
    CachedImage(
        key = "photo:$photo@$maxSizePx",
        decode = { loader.loadPhoto(photo, maxSizePx) },
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

@Composable
private fun CachedImage(
    key: String?,
    decode: suspend () -> ImageBitmap?,
    contentDescription: String?,
    modifier: Modifier,
) {
    val bitmap by produceState(initialValue = key?.let { cache.get(it) }, key) {
        if (key != null && value == null) {
            // Falha ao decodificar (arquivo apagado, formato inválido) mostra o ícone neutro.
            value = runCatching { decode() }.getOrNull()?.also { cache.put(key, it) }
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
