package com.bioscanner.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// TODO(iOS): CLLocationManager.requestWhenInUseAuthorization quando a localização do iOS existir.
// Por enquanto o registro no iOS segue sem local, como quando a permissão é negada no Android.
@Composable
actual fun rememberLocationPermissionState(onResult: (granted: Boolean) -> Unit): LocationPermissionState = remember {
    object : LocationPermissionState {
        override val isGranted: Boolean = false
        override fun request() = onResult(false)
    }
}
