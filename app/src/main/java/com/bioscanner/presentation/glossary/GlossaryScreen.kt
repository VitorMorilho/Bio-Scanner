package com.bioscanner.presentation.glossary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bioscanner.presentation.common.BioScannerBottomNav
import com.bioscanner.presentation.common.BottomNavTab
import com.bioscanner.presentation.glossary.region.RegionSection
import com.bioscanner.presentation.glossary.species.SpeciesSearchSection
import com.bioscanner.ui.theme.BorderChip
import com.bioscanner.ui.theme.ForestGreen
import com.bioscanner.ui.theme.TextSecondary

// Abas do Glossário: busca de espécies e serpentes por estado (mapa).
private enum class GlossaryTab(val label: String) {
    SPECIES("Espécies"),
    REGIONS("Por região"),
}

@Composable
fun GlossaryScreen(
    onTabClick: (BottomNavTab) -> Unit,
    onSpeciesClick: (speciesId: String) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(GlossaryTab.SPECIES) }

    Scaffold(
        bottomBar = { BioScannerBottomNav(current = BottomNavTab.GLOSSARY, onTabClick = onTabClick) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp).padding(top = 32.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "CONHEÇA AS ESPÉCIES",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = "Glossário",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ForestGreen,
                )
            }

            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = ForestGreen,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                GlossaryTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.label, style = MaterialTheme.typography.labelLarge) },
                        unselectedContentColor = TextSecondary,
                    )
                }
            }

            when (selectedTab) {
                GlossaryTab.SPECIES -> SpeciesSearchSection(
                    onSpeciesClick = onSpeciesClick,
                    modifier = Modifier.weight(1f),
                )
                GlossaryTab.REGIONS -> RegionSection(
                    onSpeciesClick = onSpeciesClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
internal fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) ForestGreen else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else ForestGreen,
        border = BorderStroke(1.5.dp, if (selected) ForestGreen else BorderChip),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}
