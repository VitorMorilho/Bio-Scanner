package com.bioscanner.presentation.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bioscanner.domain.model.RiskLevel

@Composable
fun VenomousBadge(venomous: Boolean, riskLevel: RiskLevel, modifier: Modifier = Modifier) {
    val (containerColor, label) = when {
        !venomous -> MaterialTheme.colorScheme.tertiaryContainer to "Não peçonhenta"
        riskLevel == RiskLevel.HIGH -> MaterialTheme.colorScheme.errorContainer to "Peçonhenta — risco alto"
        riskLevel == RiskLevel.MEDIUM -> MaterialTheme.colorScheme.errorContainer to "Peçonhenta — risco médio"
        riskLevel == RiskLevel.LOW -> MaterialTheme.colorScheme.errorContainer to "Peçonhenta — risco baixo"
        else -> MaterialTheme.colorScheme.surfaceVariant to "Peçonhenta — risco desconhecido"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = containerColor,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}
