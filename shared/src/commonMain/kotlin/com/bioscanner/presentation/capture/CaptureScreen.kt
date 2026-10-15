package com.bioscanner.presentation.capture

import androidx.compose.runtime.Composable

/** Câmera + galeria. A captura e a escolha de fotos são da plataforma (CameraX/Photo Picker no Android). */
@Composable
expect fun CaptureScreen(onPhotoReady: (CapturedPhoto) -> Unit)
