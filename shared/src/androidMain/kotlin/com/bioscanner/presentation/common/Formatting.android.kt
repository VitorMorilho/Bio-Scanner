package com.bioscanner.presentation.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PT_BR = Locale.forLanguageTag("pt-BR")

// SimpleDateFormat em vez de java.time: minSdk 24 não tem java.time sem desugaring.
actual fun formatDateTime(epochMillis: Long): String =
    SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", PT_BR).format(Date(epochMillis))
