package com.extremeaudiolab.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

enum class FaderCurve { LINEAR, LOG }

private fun readable(value: Float): String = when {
    abs(value) >= 100f -> value.roundToInt().toString()
    abs(value) >= 10f -> "%.1f".format(value)
    else -> "%.2f".format(value)
}

/**
 * Fader com valor numérico sempre visível e ajuda em linguagem simples.
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
    val isLog = curve == FaderCurve.LOG
    val lo = if (isLog) range.start.coerceAtLeast(0.001f) else range.start
    val hi = range.endInclusive
    val sliderRange = if (isLog) 0f..1000f else range
    val pos = if (isLog) {
        (ln((value.coerceIn(range) / lo).coerceAtLeast(1e-6f)) / ln(hi / lo) * 1000f).coerceIn(0f, 1000f)
    } else {
        value.coerceIn(range)
    }

    Column(modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = LabColors.Text,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${readable(value)}$unit",
                style = MaterialTheme.typography.labelLarge,
                color = LabColors.Accent,
                fontWeight = FontWeight.Bold,
            )
            if (description != null) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LabColors.Surface2)
                        .clickable { showHelp = !showHelp },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (showHelp) "−" else "?",
                        color = LabColors.AccentSoft,
                        fontSize = 14.sp,
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
                thumbColor = LabColors.Accent,
                activeTrackColor = LabColors.Accent,
                inactiveTrackColor = LabColors.Track,
            ),
            modifier = Modifier.fillMaxWidth().height(30.dp),
        )
        AnimatedVisibility(visible = showHelp && description != null) {
            Text(
                description ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = LabColors.TextMuted,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
            )
        }
    }
}

/** Compatibilidade com o sequenciador, que usa o nome antigo. */
@Composable
fun Knob(label: String, value: Float, range: ClosedFloatingPointRange<Float>, on: (Float) -> Unit) =
    Fader(label, value, range, on)

@Composable
fun SectionCard(
    title: String,
    subtitle: String? = null,
    icon: String = "◆",
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LabColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LabColors.Accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(icon, fontSize = 17.sp, color = LabColors.Accent)
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = LabColors.Accent)
                    if (subtitle != null) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = LabColors.TextMuted)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEachIndexed { i, name ->
            FilterChip(
                selected = selected == i,
                onClick = { onSelect(i) },
                label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LabColors.Accent.copy(alpha = 0.30f),
                    selectedLabelColor = LabColors.Accent,
                ),
            )
        }
    }
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit, danger: Boolean = false, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (danger) LabColors.Danger else LabColors.Accent,
            contentColor = if (danger) LabColors.Text else androidx.compose.ui.graphics.Color.Black,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
