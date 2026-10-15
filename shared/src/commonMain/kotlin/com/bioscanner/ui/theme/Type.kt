package com.bioscanner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.bioscanner.resources.Res
import com.bioscanner.resources.dm_sans_bold
import com.bioscanner.resources.dm_sans_medium
import com.bioscanner.resources.dm_sans_regular
import com.bioscanner.resources.fraunces_semibold
import org.jetbrains.compose.resources.Font

private val baseTypography = Typography()

/**
 * Fraunces (serifada) para títulos, DM Sans para o resto — dá o tom de
 * "caderno de campo" combinando com a paleta verde/dourado do app.
 * Composable porque as fontes vêm dos recursos multiplataforma (composeResources/font).
 */
@Composable
internal fun bioScannerTypography(): Typography {
    val fraunces = FontFamily(Font(Res.font.fraunces_semibold, FontWeight.SemiBold))
    val dmSans = FontFamily(
        Font(Res.font.dm_sans_regular, FontWeight.Normal),
        Font(Res.font.dm_sans_medium, FontWeight.Medium),
        Font(Res.font.dm_sans_bold, FontWeight.Bold),
    )
    return typography(fraunces, dmSans)
}

private fun typography(fraunces: FontFamily, dmSans: FontFamily) = Typography(
    displayLarge = baseTypography.displayLarge.copy(fontFamily = fraunces, fontWeight = FontWeight.SemiBold),
    displayMedium = baseTypography.displayMedium.copy(fontFamily = fraunces, fontWeight = FontWeight.SemiBold),
    displaySmall = baseTypography.displaySmall.copy(fontFamily = fraunces, fontWeight = FontWeight.SemiBold),
    headlineLarge = baseTypography.headlineLarge.copy(fontFamily = fraunces, fontWeight = FontWeight.SemiBold),
    headlineMedium = baseTypography.headlineMedium.copy(fontFamily = fraunces, fontWeight = FontWeight.SemiBold),
    headlineSmall = baseTypography.headlineSmall.copy(fontFamily = fraunces, fontWeight = FontWeight.SemiBold),
    titleLarge = baseTypography.titleLarge.copy(fontFamily = dmSans, fontWeight = FontWeight.Bold),
    titleMedium = baseTypography.titleMedium.copy(fontFamily = dmSans, fontWeight = FontWeight.Bold),
    titleSmall = baseTypography.titleSmall.copy(fontFamily = dmSans, fontWeight = FontWeight.Bold),
    bodyLarge = baseTypography.bodyLarge.copy(fontFamily = dmSans, fontWeight = FontWeight.Normal),
    bodyMedium = baseTypography.bodyMedium.copy(fontFamily = dmSans, fontWeight = FontWeight.Normal),
    bodySmall = baseTypography.bodySmall.copy(fontFamily = dmSans, fontWeight = FontWeight.Normal),
    labelLarge = baseTypography.labelLarge.copy(fontFamily = dmSans, fontWeight = FontWeight.Bold),
    labelMedium = baseTypography.labelMedium.copy(fontFamily = dmSans, fontWeight = FontWeight.Medium),
    labelSmall = baseTypography.labelSmall.copy(fontFamily = dmSans, fontWeight = FontWeight.Medium),
)
