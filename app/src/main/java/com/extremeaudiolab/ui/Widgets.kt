package com.extremeaudiolab.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

/** Cor do módulo atual, aplicada por SectionCard a todos os controles internos. */
val LocalModuleAccent = compositionLocalOf { LabColors.Accent }

enum class FaderCurve { LINEAR, LOG }

private fun readable(value: Float): String = when {
    abs(value) >= 100f -> value.roundToInt().toString()
    abs(value) >= 10f -> "%.1f".format(value)
    else -> "%.2f".format(value)
}

/** Parafuso pequeno, igual aos de um painel de eurorack. */
@Composable
fun Screw(size: androidx.compose.ui.unit.Dp = 11.dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF3D444B)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(size * 0.6f, 1.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Color(0xFF101418)),
        )
    }
}

/** Visor digital do valor, com a cor do módulo. */
@Composable
private fun ValueDisplay(text: String, accent: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF080A0C))
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(text, style = ValueStyle, color = accent)
    }
}

/**
 * Fader com valor sempre visível, escala e ajuda em linguagem simples.
 * Curva LOG deixa controles de frequência/tempo úteis em todo o curso do slider.
 */
@Composable
fun Fader(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    on: (Float) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    unit: String = "",
    curve: FaderCurve = FaderCurve.LINEAR,
) {
    var showHelp by remember { mutableStateOf(false) }
    val accent = LocalModuleAccent.current
    val isLog = curve == FaderCurve.LOG
    val lo = if (isLog) range.start.coerceAtLeast(0.001f) else range.start
    val hi = range.endInclusive
    val sliderRange = if (isLog) 0f..1000f else range
    val pos = if (isLog) {
        (ln((value.coerceIn(range) / lo).coerceAtLeast(1e-6f)) / ln(hi / lo) * 1000f).coerceIn(0f, 1000f)
    } else {
        value.coerceIn(range)
    }
    val frac = ((pos - sliderRange.start) / (sliderRange.endInclusive - sliderRange.start)).coerceIn(0f, 1f)

    Column(modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = LabColors.Text,
                modifier = Modifier.weight(1f),
            )
            ValueDisplay("${readable(value)}$unit", accent)
            if (description != null) {
                Spacer(Modifier.width(5.dp))
                Box(
                    Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(LabColors.Surface2)
                        .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                        .clickable { showHelp = !showHelp },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (showHelp) "−" else "?",
                        color = accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Slider(
            value = pos,
            valueRange = sliderRange,
            onValueChange = { p ->
                val next = if (isLog) lo * (hi / lo).pow(p / 1000f) else p
                on(next.coerceIn(range))
            },
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = LabColors.Track,
            ),
            modifier = Modifier.fillMaxWidth().height(28.dp),
        )
        // Marcas de escala do painel.
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            for (i in 0..8) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(accent.copy(alpha = if (i / 8f <= frac) 0.75f else 0.14f)),
                )
            }
        }
        AnimatedVisibility(visible = showHelp && description != null) {
            Text(
                description ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = LabColors.TextMuted,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            )
        }
    }
}

/** Compatibilidade com o sequenciador, que usa o nome antigo. */
@Composable
fun Knob(label: String, value: Float, range: ClosedFloatingPointRange<Float>, on: (Float) -> Unit) =
    Fader(label, value, range, on)

/** Painel de módulo: faixa de cor, parafusos e controles internos. */
@Composable
fun SectionCard(
    title: String,
    subtitle: String? = null,
    icon: String = "◆",
    accent: Color = LabColors.Accent,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(containerColor = LabColors.Surface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.30f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        CompositionLocalProvider(LocalModuleAccent provides accent) {
            Column(Modifier.padding(10.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent),
                )
                Spacer(Modifier.height(9.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Screw()
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            title.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = accent,
                        )
                        if (subtitle != null) {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = LabColors.TextMuted,
                            )
                        }
                    }
                    Text(icon, fontSize = 17.sp, color = accent.copy(alpha = 0.85f))
                    Spacer(Modifier.width(8.dp))
                    Screw()
                }
                Spacer(Modifier.height(10.dp))
                content()
            }
        }
    }
}

@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val accent = LocalModuleAccent.current
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        options.forEachIndexed { i, name ->
            FilterChip(
                selected = selected == i,
                onClick = { onSelect(i) },
                shape = RoundedCornerShape(3.dp),
                label = { Text(name.uppercase(), style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accent.copy(alpha = 0.28f),
                    selectedLabelColor = accent,
                    containerColor = LabColors.Surface2,
                    labelColor = LabColors.TextMuted,
                ),
            )
        }
    }
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit, danger: Boolean = false, enabled: Boolean = true) {
    val accent = LocalModuleAccent.current
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(3.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (danger) LabColors.Danger else accent,
            contentColor = if (danger) LabColors.Text else Color.Black,
        ),
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge)
    }
}
