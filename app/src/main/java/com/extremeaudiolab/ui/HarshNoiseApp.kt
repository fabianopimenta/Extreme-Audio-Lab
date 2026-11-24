package com.extremeaudiolab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.extremeaudiolab.model.Patch
import com.extremeaudiolab.model.Preset
import com.extremeaudiolab.model.PresetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val tabs = listOf(
    "PATCH" to "🎛",
    "SEQ" to "🥁",
    "KAOSS" to "☯",
    "GRAVAR" to "⏺",
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
        Scaffold(
            containerColor = LabColors.Background,
            topBar = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(LabColors.Accent, LabColors.Background),
                            ),
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "EXTREME AUDIO LAB",
                                style = MaterialTheme.typography.titleLarge,
                                color = LabColors.Text,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Laboratório sonoro • 100% offline",
                                style = MaterialTheme.typography.bodySmall,
                                color = LabColors.AccentSoft,
                            )
                        }
                        Box(
                            Modifier
                                .size(52.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(LabColors.Track),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(level)
                                    .fillMaxSize()
                                    .background(LabColors.Text),
                            )
                        }
                        TextButton(onClick = { showGuide = true }) {
                            Text("?", color = LabColors.Text, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = LabColors.Surface,
                    contentColor = LabColors.TextMuted,
                ) {
                    tabs.forEachIndexed { i, (label, icon) ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Text(icon) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LabColors.Accent,
                                selectedTextColor = LabColors.Accent,
                                unselectedIconColor = LabColors.TextMuted,
                                unselectedTextColor = LabColors.TextMuted,
                                indicatorColor = LabColors.Accent.copy(alpha = 0.14f),
                            ),
                        )
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

        if (showGuide) {
            AlertDialog(
                onDismissRequest = { showGuide = false },
                containerColor = LabColors.Surface,
                title = { Text("Guia rápido — comece aqui", color = LabColors.Accent) },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        GuideStep("1", "Escolha um PRESET em Patch. É o atalho para ouvir algo legal na hora.")
                        GuideStep("2", "BAIXE O VOLUME do celular antes de testar. O som pode ficar alto e agressivo.")
                        GuideStep("3", "Em PATCH, mexa no fader “Nível” (ruído) e em “Movimento” — são os que mais mudam o som.")
                        GuideStep("4", "Toque no “?” ao lado de qualquer fader para ler o que ele faz em palavras simples.")
                        GuideStep("5", "Em SEQ, ligue os quadradinhos para criar a batida. Em KAOSS, arraste o dedo pela área para mudar o efeito.")
                        GuideStep("6", "Se o microfone estiver ligado, fale ou aproxime o celular da caixa para gerar feedback.")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Dica: o fader “?” aparece só quando há ajuda. Se nada mudar ao mover um controle, verifique se o módulo dele está com o Mix em zero.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LabColors.TextMuted,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGuide = false }) {
                        Text("Entendi", color = LabColors.Accent)
                    }
                },
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
                .clip(RoundedCornerShape(7.dp))
                .background(LabColors.Accent.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(number, color = LabColors.Accent, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = LabColors.Text, modifier = Modifier.weight(1f))
    }
}
