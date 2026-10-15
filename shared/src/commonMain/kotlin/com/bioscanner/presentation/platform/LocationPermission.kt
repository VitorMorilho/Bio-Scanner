package com.bioscanner.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/** Permissão de localização (opcional: sem ela o registro segue sem local). */
@Stable
interface LocationPermissionState {
    val isGranted: Boolean

    /** Abre o pedido do sistema; o resultado chega no onResult de [rememberLocationPermissionState]. */
    fun request()
}

@Composable
expect fun rememberLocationPermissionState(onResult: (granted: Boolean) -> Unit): LocationPermissionState
