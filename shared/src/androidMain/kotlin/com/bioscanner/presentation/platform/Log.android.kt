package com.bioscanner.presentation.platform

import android.util.Log

actual fun logWarning(tag: String, message: String, throwable: Throwable?) {
    Log.w(tag, message, throwable)
}

actual fun logError(tag: String, message: String, throwable: Throwable?) {
    Log.e(tag, message, throwable)
}
