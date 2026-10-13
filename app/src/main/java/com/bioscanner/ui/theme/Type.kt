package com.bioscanner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.bioscanner.R

// Fraunces (serifada) para títulos, DM Sans para o resto — dá o tom de
// "caderno de campo" combinando com a paleta verde/dourado do app.
val Fraunces = FontFamily(
    Font(R.font.fraunces_semibold, FontWeight.SemiBold),
)

val DMSans = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_bold, FontWeight.Bold),
)

private val baseTypography = Typography()

val Typography = Typography(
    displayLarge = baseTypography.displayLarge.copy(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold),
    displayMedium = baseTypography.displayMedium.copy(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold),
    displaySmall = baseTypography.displaySmall.copy(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold),
    headlineLarge = baseTypography.headlineLarge.copy(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold),
    headlineMedium = baseTypography.headlineMedium.copy(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold),
    headlineSmall = baseTypography.headlineSmall.copy(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold),
    titleLarge = baseTypography.titleLarge.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
    titleMedium = baseTypography.titleMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
    titleSmall = baseTypography.titleSmall.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
    bodyLarge = baseTypography.bodyLarge.copy(fontFamily = DMSans, fontWeight = FontWeight.Normal),
    bodyMedium = baseTypography.bodyMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Normal),
    bodySmall = baseTypography.bodySmall.copy(fontFamily = DMSans, fontWeight = FontWeight.Normal),
    labelLarge = baseTypography.labelLarge.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
    labelMedium = baseTypography.labelMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Medium),
    labelSmall = baseTypography.labelSmall.copy(fontFamily = DMSans, fontWeight = FontWeight.Medium),
)
