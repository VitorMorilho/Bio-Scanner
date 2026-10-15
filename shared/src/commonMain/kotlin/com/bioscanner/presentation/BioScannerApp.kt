package com.bioscanner.presentation

import androidx.compose.runtime.Composable
import com.bioscanner.presentation.navigation.BioScannerNavGraph
import com.bioscanner.ui.theme.BioScannerTheme

/**
 * Raiz do app em todas as plataformas. [overlay] desenha por cima, dentro do tema (no Android,
 * o aviso de nova versão do APK).
 */
@Composable
fun BioScannerApp(overlay: @Composable () -> Unit = {}) {
    BioScannerTheme {
        BioScannerNavGraph()
        overlay()
    }
}
