package com.bioscanner.presentation.glossary.region

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bioscanner.domain.model.Region
import kotlin.math.PI
import kotlin.math.cos

/**
 * Mapa das regiões desenhado direto no Canvas (sem biblioteca de mapas, sem
 * internet). Projeção equirretangular com correção de cos(latitude média):
 * para um país do tamanho do Brasil a distorção é aceitável e a conversão
 * inversa (toque → lon/lat) fica trivial.
 */
@Composable
fun RegionMap(
    regions: List<Region>,
    selectedRegionId: String?,
    onRegionClick: (Region) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (regions.isEmpty()) return
    val projection = remember(regions) { Projection(regions) }
    val currentOnClick by rememberUpdatedState(onRegionClick)

    val fill = MaterialTheme.colorScheme.primaryContainer
    val selectedFill = MaterialTheme.colorScheme.primary
    val border = MaterialTheme.colorScheme.surface

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(projection.aspectRatio)
            .semantics { contentDescription = "Mapa do Brasil. Toque em um estado para ver suas serpentes." }
            .pointerInput(regions) {
                detectTapGestures { offset ->
                    val (lon, lat) = projection.toGeo(offset, Size(size.width.toFloat(), size.height.toFloat()))
                    regions.firstOrNull { it.contains(lon, lat) }?.let(currentOnClick)
                }
            }
            .drawWithCache {
                // Os paths só são recalculados quando o tamanho muda; trocar a
                // seleção apenas redesenha com outra cor.
                val paths = regions.associate { it.id to projection.toPath(it, size) }
                val stroke = Stroke(width = 1.dp.toPx())
                onDrawBehind {
                    paths.forEach { (id, path) ->
                        drawPath(path, if (id == selectedRegionId) selectedFill else fill)
                        drawPath(path, border, style = stroke)
                    }
                }
            },
    )
}

private class Projection(regions: List<Region>) {
    private val minLon: Float
    private val maxLon: Float
    private val minLat: Float
    private val maxLat: Float
    private val lonScale: Float

    init {
        var x0 = Float.MAX_VALUE
        var x1 = -Float.MAX_VALUE
        var y0 = Float.MAX_VALUE
        var y1 = -Float.MAX_VALUE
        regions.forEach { region ->
            region.rings.forEach { ring ->
                for (i in ring.indices step 2) {
                    x0 = minOf(x0, ring[i]); x1 = maxOf(x1, ring[i])
                    y0 = minOf(y0, ring[i + 1]); y1 = maxOf(y1, ring[i + 1])
                }
            }
        }
        minLon = x0; maxLon = x1; minLat = y0; maxLat = y1
        lonScale = cos((minLat + maxLat) / 2 * PI / 180).toFloat()
    }

    val aspectRatio: Float = (maxLon - minLon) * lonScale / (maxLat - minLat)

    private fun scale(size: Size) =
        minOf(size.width / ((maxLon - minLon) * lonScale), size.height / (maxLat - minLat))

    fun toPath(region: Region, size: Size): Path {
        val s = scale(size)
        return Path().apply {
            fillType = PathFillType.EvenOdd
            region.rings.forEach { ring ->
                for (i in ring.indices step 2) {
                    val x = (ring[i] - minLon) * lonScale * s
                    val y = (maxLat - ring[i + 1]) * s
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
        }
    }

    fun toGeo(offset: Offset, size: Size): Pair<Float, Float> {
        val s = scale(size)
        return (offset.x / (lonScale * s) + minLon) to (maxLat - offset.y / s)
    }
}
