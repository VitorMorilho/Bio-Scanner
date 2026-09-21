package com.bioscanner.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bioscanner.presentation.common.DisclaimerBanner
import com.bioscanner.presentation.common.VenomousBadge

@Composable
fun SpeciesDetailScreen(viewModel: SpeciesDetailViewModel = hiltViewModel()) {
    val species by viewModel.species.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val current = species
            if (current == null) {
                Text("Carregando...", style = MaterialTheme.typography.bodyMedium)
                return@Column
            }

            Text(text = current.popularName, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = current.scientificName,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
            )
            Text(text = current.family, style = MaterialTheme.typography.bodyMedium)
            VenomousBadge(venomous = current.venomous, riskLevel = current.riskLevel)

            Text(text = "Descrição", style = MaterialTheme.typography.titleMedium)
            Text(text = current.description, style = MaterialTheme.typography.bodyMedium)

            Text(text = "Distribuição geográfica", style = MaterialTheme.typography.titleMedium)
            Text(text = current.geographicDistribution, style = MaterialTheme.typography.bodyMedium)

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Em caso de acidente",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        text = current.firstAidProcedure,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            DisclaimerBanner()
        }
    }
}
