package com.bioscanner.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bioscanner.domain.model.Species
import com.bioscanner.presentation.common.DisclaimerBanner
import com.bioscanner.presentation.common.SpeciesImage
import com.bioscanner.presentation.common.VenomousBadge

// Usado quando a espécie é peçonhenta mas ainda não tem texto curado:
// numa ficha de serpente peçonhenta, o bloco de primeiros socorros nunca some.
private const val GENERIC_FIRST_AID =
    "Mantenha a vítima calma e imóvel, retire anéis e objetos apertados do membro picado e " +
        "procure atendimento médico imediatamente. NÃO faça torniquete, NÃO corte o local da " +
        "picada e NÃO tente sugar o veneno."

// Tipos de dentição explicados em linguagem simples, ao lado do termo técnico.
private val DENTITION_EXPLANATION = mapOf(
    "áglifa" to "sem presas de veneno",
    "opistóglifa" to "presas de veneno no fundo da boca",
    "proteróglifa" to "presas de veneno pequenas e fixas na frente da boca",
    "solenóglifa" to "presas de veneno grandes e móveis na frente da boca",
)

@Composable
fun SpeciesDetailScreen(viewModel: SpeciesDetailViewModel = hiltViewModel()) {
    val species by viewModel.species.collectAsState()
    val regionNames by viewModel.regionNames.collectAsState()

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

            if (current.referenceImagePath != null) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SpeciesImage(
                        assetPath = current.referenceImagePath,
                        contentDescription = "Foto de ${current.displayName}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                    current.imageCredit?.let {
                        Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = current.displayName, style = MaterialTheme.typography.headlineSmall)
                if (current.popularName != null) {
                    Text(
                        text = current.scientificName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                    )
                }
                Text(text = "Família ${current.family}", style = MaterialTheme.typography.bodyMedium)
                if (current.otherNames.isNotEmpty()) {
                    Text(
                        text = "Também chamada de: ${current.otherNames.joinToString()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            VenomousBadge(venomous = current.venomous, riskLevel = current.riskLevel)

            FactsCard(current)

            Section("Comportamento de defesa", current.defensiveBehavior)
            Section("Descrição", current.description)
            Section("Distribuição geográfica", current.geographicDistribution)
            if (regionNames.isNotEmpty()) {
                Section("Estados com registro", regionNames.joinToString())
            }

            val firstAid = current.firstAidProcedure ?: GENERIC_FIRST_AID.takeIf { current.venomous }
            if (firstAid != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Em caso de acidente",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            text = firstAid,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            DisclaimerBanner()
        }
    }
}

/** Alimentação, atividade e dentição: dados curtos, agrupados num cartão. */
@Composable
private fun FactsCard(species: Species) {
    val dentition = species.dentition?.let { d ->
        DENTITION_EXPLANATION[d]?.let { "${d.replaceFirstChar(Char::uppercase)} — $it" } ?: d
    }
    val facts = listOfNotNull(
        species.diet?.let { "Alimentação" to it },
        species.activityPeriod?.let { "Período de atividade" to it },
        dentition?.let { "Dentição" to it },
    )
    if (facts.isEmpty()) return

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            facts.forEach { (label, value) ->
                Column {
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, body: String?) {
    if (body.isNullOrBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = body, style = MaterialTheme.typography.bodyMedium)
    }
}
