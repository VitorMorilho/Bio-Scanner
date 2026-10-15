package com.bioscanner.presentation.records

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bioscanner.presentation.common.CaptureInfoCard
import com.bioscanner.presentation.common.ObservationPhoto
import com.bioscanner.ui.theme.ForestGreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ObservationDetailScreen(
    onDeleted: () -> Unit,
    viewModel: ObservationDetailViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.deleted.collect { onDeleted() }
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Excluir registro?") },
            text = { Text("A foto e os dados deste registro serão apagados do app. Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDelete = false
                        deleting = true
                        viewModel.delete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text("Cancelar") }
            },
        )
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
            val observation = state.observation
            // Durante a exclusão o Flow já emite null; não pisca "não encontrado" antes de voltar.
            if (deleting) return@Column
            if (observation == null) {
                Text(
                    text = if (state.loading) "Carregando..." else "Registro não encontrado.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                return@Column
            }

            Text(text = "Registro", style = MaterialTheme.typography.headlineMedium, color = ForestGreen)

            ObservationPhoto(
                photo = observation.photoPath,
                contentDescription = "Foto do registro",
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(16.dp)),
            )

            CaptureInfoCard(metadata = observation.metadata, regionName = state.regionName)

            OutlinedButton(
                onClick = { confirmingDelete = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null)
                Text("Excluir registro", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
