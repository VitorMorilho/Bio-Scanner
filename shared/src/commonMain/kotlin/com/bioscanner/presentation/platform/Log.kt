package com.bioscanner.presentation.platform

/** Log da plataforma (Logcat no Android, NSLog no iOS). */
expect fun logWarning(tag: String, message: String, throwable: Throwable? = null)

expect fun logError(tag: String, message: String, throwable: Throwable? = null)
