package com.bioscanner.presentation.common

import com.bioscanner.domain.model.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PT_BR = Locale.forLanguageTag("pt-BR")

// SimpleDateFormat em vez de java.time: minSdk 24 não tem java.time sem desugaring.
fun formatDateTime(epochMillis: Long): String =
    SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", PT_BR).format(Date(epochMillis))

fun formatCoordinates(point: GeoPoint): String =
    String.format(Locale.US, "%.5f, %.5f", point.latitude, point.longitude)
