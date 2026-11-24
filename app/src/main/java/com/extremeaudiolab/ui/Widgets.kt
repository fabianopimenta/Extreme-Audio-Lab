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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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

/** Parafuso metálico com fenda em cruz, como os de um painel eurorack real. */@Composable
fun Screw(size: Dp = 11.dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFD2D9DE), Color(0xFF7C858D), Color(0xFF2A3035)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(size * 0.68f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFB9C2C9), Color(0xFF596169), Color(0xFF181C20)),
                    ),
                ),
        )
        Box(
            Modifier
                .width(size * 0.58f)
                .height(1.dp)
                .background(Color(0xFF07090B)),
        )
        Box(
            Modifier
                .width(1.dp)
                .height(size * 0.58f)
                .background(Color(0xFF07090B)),
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
@OptIn(ExperimentalMaterial3Api::class)
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
            // Trilho fino e cap de fader: o controle deixa de parecer um slider
            // de app e passa a lembrar os faders deslizantes de um módulo real.
            track = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF0A0C0E)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(frac)
                            .fillMaxHeight()
                            .background(accent.copy(alpha = 0.55f)),
                    )
                }
            },
            thumb = {
                Box(
                    Modifier
                        .size(width = 16.dp, height = 26.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFE8ECEF), Color(0xFF98A1A8), Color(0xFF343B42)),
                            ),
                        )
                        .border(1.dp, Color(0xFF0B0F13), RoundedCornerShape(2.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.68f)
                            .height(2.dp)
                            .background(accent),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(30.dp),
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

/** Tomada de patch (jack 3,5 mm) decorativa do painel. */
@Composable
private fun Jack(size: Dp = 11.dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFBFC8CF), Color(0xFF6C757D), Color(0xFF191D21)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(size * 0.46f)
                .clip(CircleShape)
                .background(Color(0xFF050708)),
        )
    }
}

/**
 * Painel de módulo eurorack: alumínio escovado, parafusos nos quatro cantos,
 * faixa de marca no topo e etiqueta de fabricante no rodapé.
 */
@Composable
fun SectionCard(
    title: String,
    subtitle: String? = null,
    icon: String = "◆",
    accent: Color = LabColors.Accent,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF050607)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        CompositionLocalProvider(LocalModuleAccent provides accent) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                accent.copy(alpha = 0.10f),
                                Color(0xFF2B3035),
                                Color(0xFF1A1D21),
                                Color(0xFF111417),
                            ),
                        ),
                    )
                    .drawBehind {
                        // Micro-linhas horizontais: imita alumínio escovado.
                        val step = 3.dp.toPx()
                        var y = 0f
                        while (y < size.height) {
                            drawLine(
                                Color.White.copy(alpha = 0.022f),
                                Offset(0f, y), Offset(size.width, y), 1f,
                            )
                            y += step
                        }
                        drawLine(
                            Color.White.copy(alpha = 0.10f),
                            Offset(0f, 0f), Offset(0f, size.height), 1f,
                        )
                        drawLine(
                            Color.Black.copy(alpha = 0.40f),
                            Offset(size.width - 1f, 0f), Offset(size.width - 1f, size.height), 1f,
                        )
                    },
            ) {
                // Faixa de identificação do módulo.
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(accent),
                )
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 13.dp, bottom = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                title.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = accent,
                                letterSpacing = 0.7.sp,
                            )
                            if (subtitle != null) {
                                Text(
                                    subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LabColors.TextMuted,
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "PMNT",
                                fontSize = 7.sp,
                                color = LabColors.TextMuted,
                                letterSpacing = 2.sp,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(icon, fontSize = 15.sp, color = accent.copy(alpha = 0.85f))
                                Spacer(Modifier.width(6.dp))
                                Jack()
                                Spacer(Modifier.width(4.dp))
                                Jack()
                            }
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(accent.copy(alpha = 0.38f)),
                    )
                    Spacer(Modifier.height(7.dp))
                    content()
                    Spacer(Modifier.height(7.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "PMNT RESEARCH",
                            style = MaterialTheme.typography.labelSmall,
                            color = LabColors.TextMuted.copy(alpha = 0.75f),
                            letterSpacing = 1.2.sp,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "EURORACK · 12HP",
                            style = MaterialTheme.typography.labelSmall,
                            color = LabColors.TextMuted.copy(alpha = 0.55f),
                            letterSpacing = 0.8.sp,
                        )
                    }
                }
                // Quatro parafusos de montagem, um em cada canto.
                Screw(9.dp, Modifier.align(Alignment.TopStart).padding(3.dp))
                Screw(9.dp, Modifier.align(Alignment.TopEnd).padding(3.dp))
                Screw(9.dp, Modifier.align(Alignment.BottomStart).padding(3.dp))
                Screw(9.dp, Modifier.align(Alignment.BottomEnd).padding(3.dp))
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
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    danger: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val accent = LocalModuleAccent.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(3.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (danger) LabColors.Danger else accent,
            contentColor = if (danger) LabColors.Text else Color.Black,
        ),
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge)
    }
}
