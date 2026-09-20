package com.bioscanner.presentation.result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bioscanner.presentation.common.ConfidenceBadge
import com.bioscanner.presentation.common.DisclaimerBanner
import com.bioscanner.presentation.common.VenomousBadge

@Composable
fun ResultScreen(
    onViewDetails: (String) -> Unit,
    onTryAgain: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DisclaimerBanner()

            when (val current = state) {
                is ResultUiState.Loading -> LoadingContent()
                is ResultUiState.Success -> SuccessContent(current, onViewDetails)
                is ResultUiState.LowConfidence -> LowConfidenceContent(onTryAgain)
                is ResultUiState.NoMatch -> NoMatchContent(onTryAgain)
                is ResultUiState.Error -> ErrorContent(current.message, onTryAgain)
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = "Analisando imagem...",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun SuccessContent(state: ResultUiState.Success, onViewDetails: (String) -> Unit) {
    val species = state.species
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = species.popularName, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = species.scientificName,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ConfidenceBadge(confidence = state.confidence)
            VenomousBadge(venomous = species.venomous, riskLevel = species.riskLevel)
        }
        Button(
            onClick = { onViewDetails(species.id) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text("Ver detalhes da espécie")
        }
    }
}

@Composable
private fun LowConfidenceContent(onTryAgain: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Não foi possível identificar a espécie com confiança suficiente.",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Tente novamente com uma foto mais nítida, bem iluminada e próxima do animal.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onTryAgain, modifier = Modifier.fillMaxWidth()) {
            Text("Tentar novamente")
        }
    }
}

@Composable
private fun NoMatchContent(onTryAgain: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Não encontramos nenhuma espécie correspondente no banco de dados local.",
            style = MaterialTheme.typography.titleMedium,
        )
        Button(onClick = onTryAgain, modifier = Modifier.fillMaxWidth()) {
            Text("Tentar novamente")
        }
    }
}

@Composable
private fun ErrorContent(message: String, onTryAgain: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = message, style = MaterialTheme.typography.titleMedium)
        Button(onClick = onTryAgain, modifier = Modifier.fillMaxWidth()) {
            Text("Tentar novamente")
        }
    }
}
