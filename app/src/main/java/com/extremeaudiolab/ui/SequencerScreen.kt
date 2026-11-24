package com.extremeaudiolab.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.extremeaudiolab.model.Patch
import com.extremeaudiolab.model.Step

private val voices = listOf("Kick", "Snare", "Hat", "Tom", "Metal", "Burst")

@Composable
fun SequencerScreen(patch: Patch, update: (Patch) -> Unit, controller: AudioController) {
    var selected by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize().padding(10.dp)) {
        SectionCard(
            "Sequenciador — a batida",
            "Toque nos quadrados para ligar/desligar cada passo.",
            "🥁",
            EuroColors.MakeNoise,
        ) {
            Row {
                Fader(
                    "BPM", patch.bpm, 40f..300f, { update(patch.copy(bpm = it)) },
                    modifier = Modifier.weight(1f),
                    description = "Velocidade da batida. 40 é bem devagar; 300 é muito rápido.",
                )
                Fader(
                    "SWING", patch.swing, 0f..0.6f, { update(patch.copy(swing = it)) },
                    modifier = Modifier.weight(1f),
                    description = "Atrasa os passos pares, criando aquele 'balanço' de groove.",
                )
            }
            Text(
                "Quadrado aceso = passo ativo. Toque em um passo para escolher o som e ajustar pitch/força abaixo.",
                style = MaterialTheme.typography.bodySmall,
                color = LabColors.TextMuted,
            )
        }

        LazyVerticalGrid(GridCells.Fixed(4), modifier = Modifier.weight(1f).padding(vertical = 5.dp)) {
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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(3.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (st.on) EuroColors.MakeNoise else LabColors.Surface2,
                        contentColor = if (st.on) Color.Black else LabColors.TextMuted,
                    ),
                    modifier = Modifier.padding(3.dp).height(54.dp),
                ) {
                    Text("${i + 1} · ${voices[st.voice]}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        SectionCard("Passo ${selected + 1}", "Ajuste fino do passo selecionado.", "🎚", EuroColors.Intellijel) {
            Segmented(voices, patch.steps[selected].voice) { vi ->
                val next = patch.steps[selected].copy(voice = vi)
                setStep(patch, update, controller, selected, next)
            }
            Fader(
                "AFINAÇÃO", patch.steps[selected].pitch, -24f..24f,
                { setStep(patch, update, controller, selected, patch.steps[selected].copy(pitch = it)) },
                unit = " st",
                description = "Sobe ou desce o som do passo em semitons. -12 fica uma oitava abaixo.",
            )
            Fader(
                "FORÇA", patch.steps[selected].velocity, 0f..1f,
                { setStep(patch, update, controller, selected, patch.steps[selected].copy(velocity = it)) },
                description = "Quanto esse passo soa alto em relação aos outros.",
            )
            Fader(
                "CHANCE", patch.steps[selected].probability, 0f..1f,
                { setStep(patch, update, controller, selected, patch.steps[selected].copy(probability = it)) },
                description = "Chance do passo tocar de verdade. 0,5 toca mais ou menos metade das vezes.",
            )
            Button(
                onClick = {
                    val cleared = List(16) { Step() }
                    update(patch.copy(steps = cleared))
                    cleared.forEachIndexed { i, st ->
                        controller.setStep(i, st.on, st.voice, st.pitch, st.velocity, st.probability)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(3.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LabColors.Surface2,
                    contentColor = LabColors.TextMuted,
                ),
            ) { Text("LIMPAR TODOS OS PASSOS") }
        }
    }
}

private fun setStep(
    patch: Patch, update: (Patch) -> Unit, controller: AudioController, i: Int, st: Step,
) {
    val steps = patch.steps.toMutableList()
    steps[i] = st
    update(patch.copy(steps = steps))
    controller.setStep(i, st.on, st.voice, st.pitch, st.velocity, st.probability)
}
