package com.bioscanner.presentation.platform

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

@Composable
actual fun rememberLocationPermissionState(onResult: (granted: Boolean) -> Unit): LocationPermissionState {
    val context = LocalContext.current
    var granted by rememberSaveable {
        mutableStateOf(
            LOCATION_PERMISSIONS.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED },
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        granted = results.values.any { it }
        onResult(granted)
    }
    return remember(launcher) {
        object : LocationPermissionState {
            override val isGranted: Boolean get() = granted
            override fun request() = launcher.launch(LOCATION_PERMISSIONS)
        }
    }
}
