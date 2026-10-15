package com.bioscanner

import android.app.Application
import com.bioscanner.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BioScannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BioScannerApplication)
            modules(appModules)
        }
    }
}
