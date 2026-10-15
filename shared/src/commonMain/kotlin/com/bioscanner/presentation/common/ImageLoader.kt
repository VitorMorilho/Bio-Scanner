package com.bioscanner.presentation.common

import androidx.compose.ui.graphics.ImageBitmap

/** Decodifica as imagens locais do app; cada plataforma usa o decodificador nativo. */
interface ImageLoader {
    /** Foto de espécie empacotada com o app (ex.: "images/species/x.webp"). */
    suspend fun loadBundled(path: String): ImageBitmap?

    /**
     * Foto de um registro: caminho de arquivo (cópia salva pelo app) ou URI da plataforma (foto
     * recém-tirada/escolhida, antes de salvar). [maxSizePx] reduz na decodificação.
     */
    suspend fun loadPhoto(photo: String, maxSizePx: Int): ImageBitmap?
}

/** LRU simples por bytes (o LruCache do Android não existe no código comum). Só usado na main thread. */
internal class ImageCache(private val maxBytes: Long) {
    private val entries = LinkedHashMap<String, ImageBitmap>()
    private var size = 0L

    fun get(key: String): ImageBitmap? = entries.remove(key)?.also { entries[key] = it }

    fun put(key: String, image: ImageBitmap) {
        entries.remove(key)?.let { size -= it.bytes }
        entries[key] = image
        size += image.bytes
        val iterator = entries.entries.iterator()
        while (size > maxBytes && iterator.hasNext()) {
            size -= iterator.next().value.bytes
            iterator.remove()
        }
    }

    private val ImageBitmap.bytes: Long get() = width.toLong() * height * 4
}
