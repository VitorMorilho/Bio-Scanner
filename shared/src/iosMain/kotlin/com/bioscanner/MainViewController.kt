package com.bioscanner

import androidx.compose.ui.window.ComposeUIViewController
import com.bioscanner.di.initKoin
import com.bioscanner.presentation.BioScannerApp
import platform.UIKit.UIViewController

/** Ponto de entrada do app iOS (ios/iosApp/ContentView.swift). */
@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { BioScannerApp() }
}
