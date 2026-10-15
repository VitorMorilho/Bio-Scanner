package com.bioscanner

import android.app.Application
import com.bioscanner.di.appModules
import com.bioscanner.di.testOverridesModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/** Application dos testes instrumentados: os módulos do app com as substituições de teste. */
class TestBioScannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startTestKoin()
    }

    /**
     * Recria todos os singletons (inclusive o banco em memória), para cada teste começar do zero
     * como acontecia com o Hilt, que criava um grafo novo por teste.
     */
    fun resetKoin() {
        stopKoin()
        startTestKoin()
    }

    private fun startTestKoin() {
        startKoin {
            androidContext(this@TestBioScannerApplication)
            allowOverride(true)
            modules(appModules + testOverridesModule)
        }
    }
}
