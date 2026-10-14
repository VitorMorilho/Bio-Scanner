package com.bioscanner.presentation.update

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel

/** Avisa quando há um APK mais novo publicado. "Baixar" abre o link no navegador. */
@Composable
fun UpdatePrompt(viewModel: UpdateViewModel = hiltViewModel()) {
    val update by viewModel.update.collectAsState()
    val uriHandler = LocalUriHandler.current
    val available = update ?: return

    AlertDialog(
        onDismissRequest = viewModel::dismiss,
        title = { Text("Nova versão disponível") },
        text = {
            Text(
                "A versão ${available.versionName} do BioScanner já pode ser baixada. Depois do " +
                    "download, abra o arquivo para instalar por cima desta. Seus registros e sua conta continuam.",
            )
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.dismiss()
                uriHandler.openUri(available.downloadUrl)
            }) { Text("Baixar") }
        },
        dismissButton = { TextButton(onClick = viewModel::dismiss) { Text("Agora não") } },
    )
}
