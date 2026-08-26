@file:OptIn(ExperimentalTextApi::class)

package app.nodeloc.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.nodeloc.R

private val Montserrat = FontFamily(
    Font(R.font.montserrat, FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.montserrat, FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.montserrat, FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.montserrat, FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

val NodelocTypography = Typography().withFontFamily(Montserrat)

/** Montserrat 字形 + 整体字号较 Material 默认下调一档,贴近官网移动端观感 */
private fun Typography.withFontFamily(f: FontFamily) = copy(
    displayLarge = displayLarge.copy(fontFamily = f),
    displayMedium = displayMedium.copy(fontFamily = f),
    displaySmall = displaySmall.copy(fontFamily = f),
    headlineLarge = headlineLarge.copy(fontFamily = f),
    headlineMedium = headlineMedium.copy(fontFamily = f),
    headlineSmall = headlineSmall.copy(fontFamily = f, fontSize = 23.sp, lineHeight = 29.sp),
    titleLarge = titleLarge.copy(fontFamily = f, fontSize = 19.sp, lineHeight = 25.sp),
    titleMedium = titleMedium.copy(fontFamily = f, fontSize = 15.sp, lineHeight = 22.sp),
    titleSmall = titleSmall.copy(fontFamily = f, fontSize = 13.sp, lineHeight = 19.sp),
    bodyLarge = bodyLarge.copy(fontFamily = f, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = bodyMedium.copy(fontFamily = f, fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = bodySmall.copy(fontFamily = f, fontSize = 11.sp, lineHeight = 16.sp),
    labelLarge = labelLarge.copy(fontFamily = f, fontSize = 13.sp, lineHeight = 18.sp),
    labelMedium = labelMedium.copy(fontFamily = f, fontSize = 11.sp, lineHeight = 15.sp),
    labelSmall = labelSmall.copy(fontFamily = f, fontSize = 10.sp, lineHeight = 14.sp),
)
