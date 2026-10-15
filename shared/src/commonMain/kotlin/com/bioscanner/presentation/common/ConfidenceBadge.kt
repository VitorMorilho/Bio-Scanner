package com.bioscanner.presentation.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bioscanner.domain.classifier.ClassificationConfig
import kotlin.math.roundToInt

@Composable
fun ConfidenceBadge(confidence: Float, modifier: Modifier = Modifier) {
    val isConfident = confidence >= ClassificationConfig.MIN_CONFIDENCE_THRESHOLD
    val containerColor = if (isConfident) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isConfident) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = "Confiança: ${(confidence * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}
