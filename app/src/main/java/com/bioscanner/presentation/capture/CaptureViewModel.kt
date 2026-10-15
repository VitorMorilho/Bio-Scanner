package com.bioscanner.presentation.capture

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/** Foto pronta + de onde veio (define como a hora/local do registro são obtidos). */
data class CapturedPhoto(val uri: Uri, val fromCamera: Boolean)

/**
 * Não salva nem analisa nada — apenas produz a Uri da foto capturada/escolhida
 * e sinaliza a navegação para a tela de registro.
 */
class CaptureViewModel : ViewModel() {

    private val _photoReady = MutableSharedFlow<CapturedPhoto>(extraBufferCapacity = 1)
    val photoReady: SharedFlow<CapturedPhoto> = _photoReady

    fun onImageReady(uri: Uri, fromCamera: Boolean) {
        _photoReady.tryEmit(CapturedPhoto(uri, fromCamera))
    }
}
