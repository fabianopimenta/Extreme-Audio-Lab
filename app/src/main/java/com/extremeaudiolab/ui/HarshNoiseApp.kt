package com.extremeaudiolab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extremeaudiolab.model.Patch
import com.extremeaudiolab.model.Preset
import com.extremeaudiolab.model.PresetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val tabs = listOf(
    Triple("PATCH", "🎛", EuroColors.Intellijel),
    Triple("SEQ", "🥁", EuroColors.MakeNoise),
    Triple("KAOSS", "☯", EuroColors.Mutable),
    Triple("GRAVAR", "⏺", EuroColors.Buchla),
)

@Composable
fun ExtremeAudioApp() {
    val ctx = LocalContext.current
    var patch by remember { mutableStateOf(Patch()) }
    val presets = remember { mutableStateListOf<Preset>() }
    val controller = remember { AudioController(ctx) }
    var tab by remember { mutableIntStateOf(0) }
    var showGuide by remember { mutableStateOf(false) }
    var level by remember { mutableFloatStateOf(0f) }

    DisposableEffect(controller) {
        controller.bind()
        onDispose { controller.unbind() }
    }
    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) { PresetRepository.load(ctx) }
        presets.addAll(loaded)
        if (loaded.isNotEmpty()) patch = loaded.first().patch
    }
    LaunchedEffect(patch) { controller.applyPatch(patch) }
    LaunchedEffect(controller) {
        while (true) {
            val m = controller.meter()
            level = m[0].coerceIn(0f, 1f)
            delay(80)
        }
    }

    MaterialTheme(colorScheme = LabColorScheme, shapes = LabShapes, typography = LabTypography) {
        RackFrame {
            Scaffold(
                containerColor = LabColors.Background,
                topBar = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(LabColors.Surface2, LabColors.Background),
                                ),
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(8.dp, 34.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(EuroColors.Intellijel),
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "PMNT NOISE LAB",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = LabColors.Text,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "EURORACK NOISE MODULE · 100% OFFLINE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EuroColors.MakeNoise,
                                )
                            }
                            LedMeter(level)
                            Spacer(Modifier.width(8.dp))
                            Box(
                                Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(LabColors.Surface2)
                                    .clickable { showGuide = true },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("?", color = EuroColors.MakeNoise, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                bottomBar = {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .background(Brush.verticalGradient(listOf(LabColors.Surface2, LabColors.Surface)))
                            .padding(horizontal = 5.dp, vertical = 7.dp),
                    ) {
                        tabs.forEachIndexed { i, (label, icon, color) ->
                            val selected = tab == i
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (selected) color.copy(alpha = 0.16f) else Color.Transparent)
                                    .clickable { tab = i }
                                    .padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    Modifier
                                        .width(26.dp)
                                        .height(3.dp)
                                        .background(if (selected) color else LabColors.Track),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(icon, fontSize = 15.sp, color = if (selected) color else LabColors.TextMuted)
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) color else LabColors.TextMuted,
                                )
                            }
                        }
                    }
                },
            ) { pad ->
                Surface(modifier = Modifier.padding(pad).fillMaxSize(), color = LabColors.Background) {
                    when (tab) {
                        0 -> PatchScreen(patch, { patch = it }, controller, presets)
                        1 -> SequencerScreen(patch, { patch = it }, controller)
                        2 -> KaossScreen(patch, { patch = it }, controller)
                        3 -> StreamScreen(patch, controller)
                    }
                }
            }
        }
    }

    if (showGuide) {
        AlertDialog(
            onDismissRequest = { showGuide = false },
            containerColor = LabColors.Surface,
            title = { Text("GUIA RÁPIDO — COMECE AQUI", color = EuroColors.Intellijel) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    GuideStep("1", "Escolha um PRESET. O 'PULSE DEMON / Parede' é o som mais agressivo.")
                    GuideStep("2", "BAIXE O VOLUME do celular antes de testar. O som pode ficar muito alto.")
                    GuideStep("3", "Em PATCH, mexa em INTENSIDADE, MOVIMENTO e PUMP — são os que mais transformam o som.")
                    GuideStep("4", "Toque no '?' ao lado de qualquer fader para ler o que ele faz em palavras simples.")
                    GuideStep("5", "Em KAOSS, arraste o dedo pela área. Travar mantém a posição; trocar o efeito nunca apaga a escolha.")
                    GuideStep("6", "Em SEQ, toque nos quadrados para criar a batida. Use o botão KILL para silêncio imediato.")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Dica: se um fader parecer não fazer nada, verifique se o Mix do módulo dele está em zero.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LabColors.TextMuted,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showGuide = false }) {
                    Text("ENTENDI", color = EuroColors.Intellijel)
                }
            },
        )
    }
}

/** Caixa do rack: o case inteiro aparece, com orelhas de madeira e trilhos de metal. */
@Composable
private fun RackFrame(content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize().background(Color(0xFF050505))) {
        RackEar()
        Column(Modifier.weight(1f).fillMaxHeight()) {
            RackRail()
            Box(Modifier.weight(1f).fillMaxWidth()) { content() }
            RackRail()
        }
        RackEar()
    }
}

/** Laterais de madeira do case, com veios e parafusos como num rack eurorack. */
@Composable
private fun RackEar() {
    Box(
        Modifier
            .fillMaxHeight()
            .width(22.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF0B0704),
                        Color(0xFF2E1C11),
                        Color(0xFF5B3A23),
                        Color(0xFF3A2416),
                        Color(0xFF0C0805),
                    ),
                ),
            )
            .drawBehind {
                // Veios verticais da madeira.
                val step = 2.5.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        Color.Black.copy(alpha = 0.16f),
                        Offset(x, 0f), Offset(x, size.height), 0.8f,
                    )
                    x += step
                }
                // Brilho na aresta interna, onde o painel encosta.
                drawLine(
                    Color(0xFFFFD9B0).copy(alpha = 0.18f),
                    Offset(size.width - 1f, 0f), Offset(size.width - 1f, size.height), 1f,
                )
                drawLine(
                    Color.Black.copy(alpha = 0.55f),
                    Offset(0f, 0f), Offset(0f, size.height), 2f,
                )
            },
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            repeat(5) { Screw(7.dp) }
        }
    }
}

/** Trilho de alumínio com fendas de parafuso, como os trilhos de 3U. */
@Composable
private fun RackRail() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(26.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A0C0E),
                        Color(0xFF8A939B),
                        Color(0xFF5A636B),
                        Color(0xFF2A3036),
                        Color(0xFF101417),
                    ),
                ),
            )
            .drawBehind {
                val slot = 8.dp.toPx()
                val gap = 15.dp.toPx()
                val cy = size.height / 2f
                var x = gap
                while (x + slot < size.width - gap) {
                    drawRoundRect(
                        color = Color(0xFF05070A),
                        topLeft = Offset(x, cy - slot / 2f),
                        size = Size(slot, slot),
                        cornerRadius = CornerRadius(1.5f, 1.5f),
                    )
                    drawRoundRect(
                        color = Color(0xFFD5DCE2).copy(alpha = 0.35f),
                        topLeft = Offset(x, cy - slot / 2f),
                        size = Size(slot, slot),
                        cornerRadius = CornerRadius(1.5f, 1.5f),
                        style = Stroke(1f),
                    )
                    x += slot + gap
                }
            },
    ) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp)
                .background(EuroColors.Intellijel.copy(alpha = 0.55f)),
        )
    }
}

@Composable
private fun LedMeter(level: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(10) { i ->
            val on = level * 10f > i
            val color = when {
                !on -> Color(0xFF23282D)
                i >= 8 -> EuroColors.Buchla
                i >= 6 -> EuroColors.MakeNoise
                else -> EuroColors.Green
            }
            Box(
                Modifier
                    .size(6.dp, 15.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(color),
            )
        }
    }
}

@Composable
private fun GuideStep(number: String, text: String) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(EuroColors.Intellijel.copy(alpha = 0.28f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(number, color = EuroColors.Intellijel, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(7.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = LabColors.Text,
            modifier = Modifier.weight(1f),
        )
    }
}
