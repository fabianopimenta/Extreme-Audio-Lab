package com.extremeaudiolab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.extremeaudiolab.model.Patch

private val programs = listOf(
    "Filter + Delay", "Phaser + Reverb", "Flanger + Slicer", "Chorus + Delay",
    "Ring Mod + Filter", "Pitch + Reverb", "Tremolo + Dist", "Vinyl + Gate",
)

@Composable
fun KaossScreen(patch: Patch, update: (Patch) -> Unit, controller: AudioController) {
    val k = patch.kaoss
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Kaoss Pad — ${programs[k.program]}", style = MaterialTheme.typography.titleMedium)

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF111318))
                .pointerInput(k.latch) {
                    detectDragGestures { change, _ ->
                        val x = (change.position.x / size.width).coerceIn(0f, 1f)
                        val y = (change.position.y / size.height).coerceIn(0f, 1f)
                        val next = k.copy(x = x, y = y)
                        update(patch.copy(kaoss = next))
                        controller.setKaoss(next.program, next.x, next.y, next.latch, next.mix)
                    }
                },
        ) {
            Text(
                "X ${"%.2f".format(k.x)}  Y ${"%.2f".format(k.y)}",
                color = Color.White, modifier = Modifier.padding(12.dp),
            )
        }

        Row(Modifier.fillMaxWidth()) {
            programs.forEachIndexed { i, name ->
                FilterChip(
                    selected = k.program == i,
                    onClick = {
                        val next = k.copy(program = i)
                        update(patch.copy(kaoss = next))
                        controller.setKaoss(next.program, next.x, next.y, next.latch, next.mix)
                    },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.padding(2.dp),
                )
            }
        }
        Row {
            Switch(checked = k.latch, onCheckedChange = {
                val next = k.copy(latch = it)
                update(patch.copy(kaoss = next))
                controller.setKaoss(next.program, next.x, next.y, next.latch, next.mix)
            })
            Text("Latch")
        }
        Knob("Kaoss mix", k.mix, 0f..1f) {
            val next = k.copy(mix = it)
            update(patch.copy(kaoss = next))
            controller.setKaoss(next.program, next.x, next.y, next.latch, next.mix)
        }
    }
}
