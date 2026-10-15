package com.bioscanner.presentation.glossary.region

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bioscanner.presentation.common.DisclaimerBanner
import com.bioscanner.presentation.common.SpeciesCard
import com.bioscanner.presentation.glossary.CategoryChip
import com.bioscanner.ui.theme.ForestGreen
import com.bioscanner.ui.theme.TextSecondary
import org.koin.compose.viewmodel.koinViewModel

// Posição da fileira de siglas no LazyColumn (depois do aviso e do mapa).
private const val UF_CHIPS_ITEM_INDEX = 2

@Composable
fun RegionSection(
    onSpeciesClick: (speciesId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegionViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    RegionContent(
        state = state,
        onRegionClick = viewModel::selectRegion,
        onVenomousOnlyChange = viewModel::setVenomousOnly,
        onSpeciesClick = onSpeciesClick,
        modifier = modifier,
    )
}

@Composable
private fun RegionContent(
    state: RegionUiState,
    onRegionClick: (String) -> Unit,
    onVenomousOnlyChange: (Boolean) -> Unit,
    onSpeciesClick: (speciesId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.loading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val listState = rememberLazyListState()
    // Ao escolher um estado, rola até a fileira de siglas: o mapa sai da tela,
    // a lista de espécies aparece e dá para trocar de estado pelas siglas.
    // Compara com a última seleção vista para não rolar ao recriar a tela.
    val selectedId = state.selectedRegion?.id
    var lastSelectedId by rememberSaveable { mutableStateOf(selectedId) }
    LaunchedEffect(selectedId) {
        if (selectedId != null && selectedId != lastSelectedId) {
            listState.animateScrollToItem(UF_CHIPS_ITEM_INDEX)
        }
        lastSelectedId = selectedId
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            DisclaimerBanner(
                text = "A lista mostra onde já houve registros, não garante que outras espécies " +
                    "não ocorram. Em caso de picada, procure atendimento médico imediatamente.",
            )
        }
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                RegionMap(
                    regions = state.regions,
                    selectedRegionId = state.selectedRegion?.id,
                    onRegionClick = { onRegionClick(it.id) },
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        item {
            // Alternativa ao toque no mapa: estados pequenos (DF, SE, AL) são
            // difíceis de acertar no celular, e o Canvas não é acessível.
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.regions, key = { it.id }) { region ->
                    CategoryChip(label = region.id, selected = region.id == state.selectedRegion?.id) {
                        onRegionClick(region.id)
                    }
                }
            }
        }

        val selected = state.selectedRegion
        if (selected == null) {
            item {
                Text(
                    text = "Toque em um estado no mapa para ver as serpentes registradas nele.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        } else {
            item(key = "header") {
                RegionHeader(
                    regionName = selected.name,
                    shown = state.species.size,
                    total = state.totalSpeciesInRegion,
                    venomousOnly = state.venomousOnly,
                    onVenomousOnlyChange = onVenomousOnlyChange,
                )
            }
            items(state.species, key = { it.species.id }) { item ->
                SpeciesCard(
                    species = item.species,
                    caption = "${item.species.family.uppercase()} · ${item.occurrences} registros",
                    onClick = { onSpeciesClick(item.species.id) },
                )
            }
        }

        item {
            Text(
                text = "Fonte: ${state.source}. Registros com coordenadas, extraídos em " +
                    "${formatDate(state.generatedAt)}. Fotos: iNaturalist, com crédito na ficha de cada espécie.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun RegionHeader(
    regionName: String,
    shown: Int,
    total: Int,
    venomousOnly: Boolean,
    onVenomousOnlyChange: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = regionName, style = MaterialTheme.typography.headlineSmall, color = ForestGreen)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (venomousOnly) "$shown de $total espécies" else "$total espécies registradas",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Só peçonhentas", style = MaterialTheme.typography.labelLarge)
                Switch(checked = venomousOnly, onCheckedChange = onVenomousOnlyChange)
            }
        }
    }
}

/** "2026-10-05" → "05/10/2026"; devolve como veio se o formato for outro. */
private fun formatDate(isoDate: String): String =
    isoDate.split("-").takeIf { it.size == 3 }?.reversed()?.joinToString("/") ?: isoDate
