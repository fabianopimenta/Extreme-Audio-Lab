package com.extremeaudiolab.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Paleta única do laboratório — usada por todas as telas. */
object LabColors {
    val Background = Color(0xFF080B10)
    val Surface = Color(0xFF131A23)
    val Surface2 = Color(0xFF1B2531)
    val Accent = Color(0xFFFF6B35)
    val AccentSoft = Color(0xFFFFC2A3)
    val Text = Color(0xFFF2F5F8)
    val TextMuted = Color(0xFF9FB0C0)
    val Track = Color(0xFF2A3543)
    val Danger = Color(0xFFFF3B5C)
    val Good = Color(0xFF35D0A5)
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
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
)

val LabTypography = Typography(
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp),
    bodySmall = TextStyle(fontSize = 12.sp),
)
