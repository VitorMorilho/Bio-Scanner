package com.bioscanner.presentation.common

import com.bioscanner.domain.model.GeoPoint
import kotlin.math.abs
import kotlin.math.roundToLong

/** "15/10/2026 às 12:34", no fuso do aparelho. */
expect fun formatDateTime(epochMillis: Long): String

fun formatCoordinates(point: GeoPoint): String =
    "${formatDecimal(point.latitude, 5)}, ${formatDecimal(point.longitude, 5)}"

/** Ponto como separador, como no String.format(Locale.US) usado antes (sem String.format no código comum). */
internal fun formatDecimal(value: Double, decimals: Int): String {
    var factor = 1L
    repeat(decimals) { factor *= 10 }
    val scaled = (abs(value) * factor).roundToLong()
    val sign = if (value < 0 && scaled != 0L) "-" else ""
    val fraction = (scaled % factor).toString().padStart(decimals, '0')
    return "$sign${scaled / factor}.$fraction"
}
