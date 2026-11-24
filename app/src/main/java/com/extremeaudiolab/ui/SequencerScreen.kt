package com.extremeaudiolab.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.extremeaudiolab.model.Patch
import com.extremeaudiolab.model.Step

@Composable
fun SequencerScreen(patch: Patch, update: (Patch) -> Unit, controller: AudioController) {
    var selected by remember { mutableIntStateOf(0) }
    val voices = listOf("Kick", "Snare", "Hat", "Tom", "Industrial", "Burst")

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row {
            Knob("BPM", patch.bpm, 40f..300f) { v -> update(patch.copy(bpm = v)) }
            Knob("Swing", patch.swing, 0f..0.6f) { v -> update(patch.copy(swing = v)) }
        }
        LazyVerticalGrid(GridCells.Fixed(8), modifier = Modifier.weight(1f)) {
            items(16) { i ->
                val st = patch.steps[i]
                Button(
                    onClick = {
                        selected = i
                        val next = st.copy(on = !st.on)
                        val steps = patch.steps.toMutableList()
                        steps[i] = next
                        update(patch.copy(steps = steps))
                        controller.setStep(i, next.on, next.voice, next.pitch, next.velocity, next.probability)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (st.on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier.padding(3.dp).height(52.dp),
                ) {
                    Column {
                        Text("${i + 1}", style = MaterialTheme.typography.labelSmall)
                        Text(voices[st.voice], style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        val st = patch.steps[selected]
        Text("Passo ${selected + 1}", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            voices.forEachIndexed { vi, name ->
                FilterChip(
                    selected = st.voice == vi,
                    onClick = {
                        val next = st.copy(voice = vi)
                        val steps = patch.steps.toMutableList(); steps[selected] = next
                        update(patch.copy(steps = steps))
                        controller.setStep(selected, next.on, next.voice, next.pitch, next.velocity, next.probability)
                    },
                    label = { Text(name) },
                    modifier = Modifier.padding(2.dp),
                )
            }
        }
        Knob("Pitch (semitons)", st.pitch, -24f..24f) { v -> setStep(patch, update, controller, selected, st.copy(pitch = v)) }
        Knob("Velocity", st.velocity, 0f..1f) { v -> setStep(patch, update, controller, selected, st.copy(velocity = v)) }
        Knob("Probability", st.probability, 0f..1f) { v -> setStep(patch, update, controller, selected, st.copy(probability = v)) }
    }
}

private fun setStep(
    patch: Patch, update: (Patch) -> Unit, controller: AudioController, i: Int, st: Step,
) {
    val steps = patch.steps.toMutableList(); steps[i] = st
    update(patch.copy(steps = steps))
    controller.setStep(i, st.on, st.voice, st.pitch, st.velocity, st.probability)
}
