package com.bioscanner.presentation.capture

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.koin.compose.viewmodel.koinViewModel
import java.util.concurrent.Executor

@Composable
actual fun CaptureScreen(onPhotoReady: (CapturedPhoto) -> Unit) {
    val viewModel: CaptureViewModel = koinViewModel()
    val context = LocalContext.current
    val permissionState = rememberCameraPermissionState()
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.onImageReady(uri.toString(), fromCamera = false)
    }

    LaunchedEffect(Unit) {
        viewModel.photoReady.collect(onPhotoReady)
    }

    Scaffold { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (permissionState.isGranted) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    CameraPreviewView(
                        onImageCaptureReady = { imageCapture = it },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Button(
                    onClick = {
                        imageCapture?.let { capture ->
                            takePicture(context, capture) { uri -> viewModel.onImageReady(uri.toString(), fromCamera = true) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                ) {
                    Text("Tirar foto")
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(24.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Precisamos da permissão de câmera para tirar a foto. " +
                            "Você ainda pode escolher uma foto da galeria abaixo.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(
                        onClick = { permissionState.request() },
                        modifier = Modifier.padding(top = 16.dp),
                    ) {
                        Text("Conceder permissão de câmera")
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Text("Escolher da galeria")
            }
        }
    }
}

private fun takePicture(
    context: android.content.Context,
    imageCapture: ImageCapture,
    onSaved: (Uri) -> Unit,
) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "bioscanner_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BioScanner")
    }
    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues,
    ).build()

    val executor: Executor = ContextCompat.getMainExecutor(context)
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                outputFileResults.savedUri?.let(onSaved)
            }

            override fun onError(exception: ImageCaptureException) {
                // Erro de captura silencioso nesta primeira versão; a tela
                // permanece em pé para o usuário tentar novamente.
            }
        },
    )
}
