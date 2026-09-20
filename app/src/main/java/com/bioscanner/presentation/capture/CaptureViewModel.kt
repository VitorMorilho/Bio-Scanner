package com.bioscanner.presentation.capture

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

/**
 * Não chama o classificador — apenas produz a Uri da foto capturada/escolhida
 * e sinaliza a navegação para a tela de resultado.
 */
@HiltViewModel
class CaptureViewModel @Inject constructor() : ViewModel() {

    private val _navigateToResult = MutableSharedFlow<Uri>(extraBufferCapacity = 1)
    val navigateToResult: SharedFlow<Uri> = _navigateToResult

    fun onImageReady(uri: Uri) {
        _navigateToResult.tryEmit(uri)
    }
}
