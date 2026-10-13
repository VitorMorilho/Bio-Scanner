package com.bioscanner.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bioscanner.presentation.common.BioScannerBottomNav
import com.bioscanner.presentation.common.BottomNavTab
import com.bioscanner.presentation.common.DisclaimerBanner
import com.bioscanner.ui.theme.AccentGold
import com.bioscanner.ui.theme.ForestGreen
import com.bioscanner.ui.theme.TextSecondary

@Composable
fun HomeScreen(onCaptureClick: () -> Unit, onTabClick: (BottomNavTab) -> Unit) {
    Scaffold(
        bottomBar = { BioScannerBottomNav(current = BottomNavTab.REGISTER, onTabClick = onTabClick) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "GUIA DE CAMPO",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = "O que você encontrou?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ForestGreen,
                )
            }

            CaptureCard(onClick = onCaptureClick)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onCaptureClick,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Câmera", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onCaptureClick,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, ForestGreen),
                ) {
                    Text("Galeria", fontWeight = FontWeight.Bold, color = ForestGreen)
                }
            }

            Text(
                text = "Cada foto vira um registro com data, hora e local, salvo em Meus registros.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            DisclaimerBanner()
        }
    }
}

@Composable
private fun CaptureCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(300.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, AccentGold),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(shape = CircleShape, color = ForestGreen, modifier = Modifier.size(84.dp)) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(22.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(text = "Toque para registrar", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Fotografe a serpente de uma distância segura. Nunca se aproxime para tirar a foto.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
