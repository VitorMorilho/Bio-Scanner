package com.bioscanner.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class BottomNavTab(val label: String, val icon: ImageVector) {
    REGISTER("Registrar", Icons.Outlined.CenterFocusStrong),
    GLOSSARY("Glossário", Icons.AutoMirrored.Outlined.MenuBook),
    RECORDS("Meus registros", Icons.Outlined.AccountCircle),
}

/** Navegação inferior entre as telas raiz. */
@Composable
fun BioScannerBottomNav(
    current: BottomNavTab,
    onTabClick: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                BottomNavTab.entries.forEach { tab ->
                    BottomNavItem(
                        icon = tab.icon,
                        label = tab.label,
                        selected = current == tab,
                        onClick = { if (tab != current) onTabClick(tab) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color)
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}
