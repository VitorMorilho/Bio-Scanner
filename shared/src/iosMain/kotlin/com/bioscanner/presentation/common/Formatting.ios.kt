package com.bioscanner.presentation.common

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun formatDateTime(epochMillis: Long): String {
    val formatter = NSDateFormatter().apply {
        locale = NSLocale(localeIdentifier = "pt_BR")
        dateFormat = "dd/MM/yyyy 'às' HH:mm"
    }
    return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0))
}
