package com.bioscanner.presentation.capture

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Estado observável de permissão de câmera, junto com um launcher pronto
 * para solicitar a permissão ao usuário.
 */
class CameraPermissionState(
    granted: Boolean,
    private val launcher: androidx.activity.result.ActivityResultLauncher<String>,
) {
    var isGranted by mutableStateOf(granted)
        private set

    fun updateGranted(granted: Boolean) {
        isGranted = granted
    }

    fun request() = launcher.launch(Manifest.permission.CAMERA)
}

@Composable
fun rememberCameraPermissionState(): CameraPermissionState {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
    }
    val state = remember(launcher) { CameraPermissionState(granted, launcher) }
    state.updateGranted(granted)
    return state
}
