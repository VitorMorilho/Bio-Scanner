package com.bioscanner.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.model.MetadataOrigin

/** Data/hora e local de um registro, dizendo de onde veio cada informação. */
@Composable
fun CaptureInfoCard(
    metadata: CaptureMetadata,
    regionName: String?,
    modifier: Modifier = Modifier,
    locationFallback: @Composable () -> Unit = {
        Text("Sem localização", style = MaterialTheme.typography.bodyMedium)
    },
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoRow(
                label = "Data e hora",
                value = formatDateTime(metadata.takenAt),
                note = when (metadata.takenAtOrigin) {
                    MetadataOrigin.PHOTO -> "lida da foto"
                    MetadataOrigin.DEVICE -> "do aparelho, no registro"
                },
            )
            val location = metadata.location
            if (location != null) {
                InfoRow(
                    label = "Local",
                    value = listOfNotNull(regionName, formatCoordinates(location)).joinToString(" · "),
                    note = when (metadata.locationOrigin) {
                        MetadataOrigin.PHOTO -> "lido da foto"
                        else -> "posição do aparelho no registro"
                    },
                )
            } else {
                Column {
                    InfoLabel("Local")
                    locationFallback()
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, note: String) {
    Column {
        InfoLabel(label)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(text = note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InfoLabel(label: String) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}
