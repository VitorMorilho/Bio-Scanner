package com.bioscanner.presentation.platform

import platform.Foundation.NSLog

actual fun logWarning(tag: String, message: String, throwable: Throwable?) {
    NSLog("W/%s: %s %s", tag, message, throwable?.stackTraceToString() ?: "")
}

actual fun logError(tag: String, message: String, throwable: Throwable?) {
    NSLog("E/%s: %s %s", tag, message, throwable?.stackTraceToString() ?: "")
}
