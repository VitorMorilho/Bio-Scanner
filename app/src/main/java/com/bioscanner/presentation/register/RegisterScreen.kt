package com.bioscanner.presentation.register

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.bioscanner.presentation.common.CaptureInfoCard
import com.bioscanner.presentation.common.DisclaimerBanner
import com.bioscanner.presentation.common.ObservationPhoto
import com.bioscanner.ui.theme.ForestGreen

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

@Composable
fun RegisterScreen(
    onSaved: () -> Unit,
    onRetake: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var locationGranted by rememberSaveable {
        mutableStateOf(
            LOCATION_PERMISSIONS.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED },
        )
    }
    var askedOnce by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        locationGranted = results.values.any { it }
        viewModel.onLocationPermissionResult(locationGranted)
    }

    // Pede a localização uma vez ao abrir; se negar, o registro segue sem ela.
    LaunchedEffect(Unit) {
        if (!locationGranted && !askedOnce) {
            askedOnce = true
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.saved.collect { onSaved() }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Novo registro", style = MaterialTheme.typography.headlineMedium, color = ForestGreen)

            ObservationPhoto(
                photo = state.photoUri,
                contentDescription = "Foto do registro",
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(16.dp)),
            )

            val metadata = state.metadata
            if (state.loadingMetadata || metadata == null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("Obtendo data, hora e localização…", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                CaptureInfoCard(
                    metadata = metadata,
                    regionName = state.regionName,
                    locationFallback = {
                        Column {
                            Text(
                                text = if (locationGranted) {
                                    "Não foi possível obter a localização (GPS desligado ou sem sinal)."
                                } else {
                                    "Localização não autorizada. O registro será salvo sem o local."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (!locationGranted) {
                                TextButton(onClick = { permissionLauncher.launch(LOCATION_PERMISSIONS) }) {
                                    Text("Permitir localização")
                                }
                            }
                        }
                    },
                )
            }

            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = viewModel::send,
                enabled = !state.loadingMetadata && !state.sending,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (state.sending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Salvar nos meus registros", fontWeight = FontWeight.Bold)
                }
            }
            OutlinedButton(
                onClick = onRetake,
                enabled = !state.sending,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Tirar outra foto")
            }

            DisclaimerBanner(
                text = "Nunca se aproxime de uma serpente para fotografar. Em caso de picada, " +
                    "procure atendimento médico imediatamente.",
            )
        }
    }
}
