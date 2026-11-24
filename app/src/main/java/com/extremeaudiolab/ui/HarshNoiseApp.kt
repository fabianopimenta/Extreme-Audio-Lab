package com.extremeaudiolab.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.extremeaudiolab.model.Patch
import com.extremeaudiolab.model.Preset
import com.extremeaudiolab.model.PresetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val LabBackground = Color(0xFF090C10)
private val LabSurface = Color(0xFF151C24)
private val LabAccent = Color(0xFFFF6B35)

@Composable
fun ExtremeAudioApp() {
    val ctx = LocalContext.current
    var patch by remember { mutableStateOf(Patch()) }
    var presets by remember { mutableStateOf(emptyList<Preset>()) }
    val controller = remember { AudioController(ctx) }
    var tab by remember { mutableIntStateOf(0) }

    DisposableEffect(controller) {
        controller.bind()
        onDispose { controller.unbind() }
    }
    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) { PresetRepository.load(ctx) }
        presets = loaded
        if (loaded.isNotEmpty()) patch = loaded.first().patch
    }
    LaunchedEffect(patch) { controller.applyPatch(patch) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = LabBackground,
            surface = LabSurface,
            primary = LabAccent,
            onPrimary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White,
        ),
    ) {
        Scaffold(
            containerColor = LabBackground,
            bottomBar = {
                NavigationBar(containerColor = LabSurface) {
                    val items = listOf("Patch", "Seq", "Kaoss", "Stream")
                    items.forEachIndexed { i, label ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            label = { Text(label) },
                            icon = { Text(label.first().toString()) },
                        )
                    }
                }
            },
        ) { pad ->
            Surface(modifier = Modifier.padding(pad), color = LabBackground) {
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
