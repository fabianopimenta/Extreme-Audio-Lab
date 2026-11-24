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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    Row(Modifier.fillMaxSize().background(LabColors.Background)) {
        RackEar()
        Column(Modifier.weight(1f).fillMaxHeight()) {
            RackRail()
            Box(Modifier.weight(1f).fillMaxWidth()) { content() }
            RackRail()
        }
        RackEar()
    }
}

/** Laterais visíveis do case, com parafusos como num rack eurorack real. */
@Composable
private fun RackEar() {
    Box(
        Modifier
            .fillMaxHeight()
            .width(18.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF100B08), LabColors.Wood, Color(0xFF3A2618), Color(0xFF0E0A07)),
                ),
            ),
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            repeat(7) { Screw(8.dp) }
        }
    }
}

@Composable
private fun RackRail() {
    Column(
        Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Brush.verticalGradient(listOf(Color(0xFF6B747C), LabColors.Rail, Color(0xFF1B2025)))),
    ) {
        Row(
            Modifier.fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(9) { Screw(8.dp) }
        }
        Box(
            Modifier
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
