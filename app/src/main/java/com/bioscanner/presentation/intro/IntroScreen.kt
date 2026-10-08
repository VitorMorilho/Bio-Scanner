package com.bioscanner.presentation.intro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.bioscanner.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

// Cores das bordas de cima/baixo da arte (amostradas da imagem), para as faixas
// que sobram em telas mais altas que 2:3 continuarem a imagem sem emenda.
private val TOP_EDGE = Color(0xFF182609)
private val BOTTOM_EDGE = Color(0xFF102A15)

private const val FADE_IN_MS = 500
private const val HOLD_MS = 1_800L
private const val FADE_OUT_MS = 400

/**
 * Tela de abertura com a arte do app. Aparece só ao abrir o app (é o início do
 * NavHost e sai do back stack ao terminar). Tocar na tela pula a espera.
 *
 * A imagem é 2:3 e os celulares são bem mais altos: ela é ajustada pela largura
 * (Fit) para não cortar os ícones das laterais, e as faixas acima/abaixo são
 * preenchidas com as cores das bordas, com degradê sobre a imagem.
 */
@Composable
fun IntroScreen(onFinished: () -> Unit) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    val alpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    // Lembrada entre recomposições: timer e toque podem chamar finish() ao mesmo tempo.
    val finished = remember { AtomicBoolean(false) }

    fun finish() {
        if (!finished.compareAndSet(false, true)) return
        scope.launch {
            alpha.animateTo(0f, tween(FADE_OUT_MS))
            currentOnFinished()
        }
    }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(FADE_IN_MS))
        delay(HOLD_MS)
        finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TOP_EDGE, BOTTOM_EDGE)))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { finish() },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.intro_splash),
            contentDescription = "Serpentes — registre, compartilhe, conserve",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { this.alpha = alpha.value }
                .drawWithContent {
                    drawContent()
                    // Funde as bordas da imagem com as faixas de cor.
                    val fade = size.height * 0.06f
                    drawRect(
                        Brush.verticalGradient(listOf(TOP_EDGE, Color.Transparent), startY = 0f, endY = fade),
                    )
                    drawRect(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, BOTTOM_EDGE),
                            startY = size.height - fade,
                            endY = size.height,
                        ),
                    )
                },
        )
    }
}
