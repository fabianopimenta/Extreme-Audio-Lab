package com.extremeaudiolab.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Paletas inspiradas nas marcas clássicas de eurorack.
 * Cada módulo da interface usa uma cor, como um painel real dentro do case.
 */
object EuroColors {
    val MakeNoise = Color(0xFFF2C14E)   // amarelo
    val Intellijel = Color(0xFFFF6B1A)  // laranja
    val Mutable = Color(0xFF4FC3E8)     // azul/ciano
    val Buchla = Color(0xFFE0323C)      // vermelho
    val Doepfer = Color(0xFF9AA7B2)     // cinza azulado
    val Erica = Color(0xFFA66BF2)       // roxo
    val WhitePanel = Color(0xFFECEAE3)  // branco (2hp/4ms)
    val Green = Color(0xFF35D0A5)
}

object LabColors {
    val Background = Color(0xFF090A0C)
    val Surface = Color(0xFF16191D)
    val Surface2 = Color(0xFF22272D)
    val Accent = EuroColors.Intellijel
    val AccentSoft = EuroColors.MakeNoise
    val Text = Color(0xFFF3F1EA)
    val TextMuted = Color(0xFF9AA2AB)
    val Track = Color(0xFF343B43)
    val Danger = EuroColors.Buchla
    val Good = EuroColors.Green
    val Rail = Color(0xFF4A5157)
    val Wood = Color(0xFF2A1C14)
}

val LabColorScheme: ColorScheme = darkColorScheme(
    primary = LabColors.Accent,
    onPrimary = Color.Black,
    primaryContainer = LabColors.Accent.copy(alpha = 0.25f),
    onPrimaryContainer = LabColors.AccentSoft,
    secondary = LabColors.AccentSoft,
    background = LabColors.Background,
    onBackground = LabColors.Text,
    surface = LabColors.Surface,
    onSurface = LabColors.Text,
    surfaceVariant = LabColors.Surface2,
    onSurfaceVariant = LabColors.TextMuted,
    error = LabColors.Danger,
)

val LabShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
)

val LabTypography = Typography(
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 10.sp),
    bodySmall = TextStyle(fontSize = 11.sp),
)

/** Fonte "display" para os valores dos faders, como um visor de módulo. */
val ValueStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 12.sp,
    fontWeight = FontWeight.Bold,
)
