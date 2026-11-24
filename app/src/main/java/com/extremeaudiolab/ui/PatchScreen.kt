package com.extremeaudiolab.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.extremeaudiolab.model.Patch
import com.extremeaudiolab.model.Preset

@Composable
fun PatchScreen(
    patch: Patch,
    update: (Patch) -> Unit,
    controller: AudioController,
    presets: List<Preset> = emptyList(),
) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
        Button(onClick = { expanded = true }) { Text("Presets (${presets.size})") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            presets.forEach { p ->
                DropdownMenuItem(
                    text = { Text(p.name) },
                    onClick = { update(p.patch); expanded = false },
                )
            }
        }
        val current = presets.firstOrNull { it.patch == patch }
        if (current != null) {
            Text(current.description, style = MaterialTheme.typography.bodySmall)
        }

        Text("Fonte de ruído", style = MaterialTheme.typography.titleMedium)
        Knob("Nível", patch.noiseLevel, 0f..1f) { update(patch.copy(noiseLevel = it)) }
        Knob("Crush (bits)", patch.noiseCrush, 2f..16f) { update(patch.copy(noiseCrush = it)) }
        Knob("S&H (Hz)", patch.noiseHold, 1f..8000f) { update(patch.copy(noiseHold = it)) }
        Knob("Comb", patch.noiseComb, 0f..1f) { update(patch.copy(noiseComb = it)) }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Microfone / Feedback", style = MaterialTheme.typography.titleMedium)
        Knob("Gain", patch.micGain, 0f..0.95f) { update(patch.copy(micGain = it)) }
        Knob("Delay (ms)", patch.micDelayMs, 1f..200f) { update(patch.copy(micDelayMs = it)) }
        Knob("Tone (Hz)", patch.micTone, 100f..8000f) { update(patch.copy(micTone = it)) }
        Knob("Mix", patch.micMix, 0f..1f) { update(patch.copy(micMix = it)) }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Hyper Fuzz", style = MaterialTheme.typography.titleMedium)
        Knob("Drive", patch.fuzzDrive, 0f..1f) { update(patch.copy(fuzzDrive = it)) }
        Knob("Octave", patch.fuzzOctave, 0f..1f) { update(patch.copy(fuzzOctave = it)) }
        Knob("Gate", patch.fuzzGate, 0f..1f) { update(patch.copy(fuzzGate = it)) }
        Knob("Tone", patch.fuzzTone, 0f..1f) { update(patch.copy(fuzzTone = it)) }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("DOD Death Metal", style = MaterialTheme.typography.titleMedium)
        Knob("Gain", patch.dodGain, 0f..1f) { update(patch.copy(dodGain = it)) }
        Knob("Low cut", patch.dodLowCut, 0f..1f) { update(patch.copy(dodLowCut = it)) }
        Knob("Mid scoop", patch.dodScoop, 0f..1f) { update(patch.copy(dodScoop = it)) }
        Knob("Presence", patch.dodPresence, 0f..1f) { update(patch.copy(dodPresence = it)) }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Glitch / Breakdown", style = MaterialTheme.typography.titleMedium)
        val modes = listOf("Stutter", "Reverse", "Freeze", "Decimate")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            modes.forEachIndexed { i, name ->
                FilterChip(
                    selected = patch.glitchMode == i,
                    onClick = { update(patch.copy(glitchMode = i)) },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
        Knob("Clock (Hz)", patch.glitchRate, 0.5f..40f) { update(patch.copy(glitchRate = it)) }
        Knob("Slice (ms)", patch.glitchSize, 10f..400f) { update(patch.copy(glitchSize = it)) }
        Knob("Probability", patch.glitchProbability, 0f..1f) { update(patch.copy(glitchProbability = it)) }
        Knob("Decimate (x)", patch.glitchDecimate, 1f..64f) { update(patch.copy(glitchDecimate = it)) }
        Knob("Bits", patch.glitchBits, 2f..16f) { update(patch.copy(glitchBits = it)) }
        Knob("Mix", patch.glitchMix, 0f..1f) { update(patch.copy(glitchMix = it)) }
        Knob("Breakdown gate", patch.glitchGate, 0f..1f) { update(patch.copy(glitchGate = it)) }
        Button(onClick = { controller.triggerGlitch() }) { Text("Disparar glitch") }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Modal Resonator", style = MaterialTheme.typography.titleMedium)
        Knob("Tune (Hz)", patch.modalTune, 40f..2000f) { update(patch.copy(modalTune = it)) }
        Knob("Inharmonicity", patch.modalInharmonicity, 0f..1f) { update(patch.copy(modalInharmonicity = it)) }
        Knob("Decay", patch.modalDecay, 0f..1f) { update(patch.copy(modalDecay = it)) }
        Knob("Brightness", patch.modalBrightness, 0f..1f) { update(patch.copy(modalBrightness = it)) }
        Knob("Mix", patch.modalMix, 0f..1f) { update(patch.copy(modalMix = it)) }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Granular", style = MaterialTheme.typography.titleMedium)
        val granModes = listOf("Scatter", "Freeze", "Reverse", "PitchDrop")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            granModes.forEachIndexed { i, name ->
                FilterChip(
                    selected = patch.granularMode == i,
                    onClick = { update(patch.copy(granularMode = i)) },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
        Knob("Grain (ms)", patch.granularSize, 20f..800f) { update(patch.copy(granularSize = it)) }
        Knob("Density (/s)", patch.granularDensity, 1f..80f) { update(patch.copy(granularDensity = it)) }
        Knob("Pitch (st)", patch.granularPitch, -24f..24f) { update(patch.copy(granularPitch = it)) }
        Knob("Spread", patch.granularSpread, 0f..1f) { update(patch.copy(granularSpread = it)) }
        Knob("Feedback", patch.granularFeedback, 0f..0.85f) { update(patch.copy(granularFeedback = it)) }
        Knob("Mix", patch.granularMix, 0f..1f) { update(patch.copy(granularMix = it)) }
        Button(onClick = { controller.triggerGranular() }) { Text("Disparar grãos") }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Vocoder / Formantes", style = MaterialTheme.typography.titleMedium)
        val sources = listOf("Ruído", "Osc", "Ambos")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            sources.forEachIndexed { i, name ->
                FilterChip(
                    selected = patch.vocoderSource == i,
                    onClick = { update(patch.copy(vocoderSource = i)) },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
        Knob("Carrier (Hz)", patch.vocoderPitch, 40f..1200f) { update(patch.copy(vocoderPitch = it)) }
        Knob("Shift (st)", patch.vocoderShift, -24f..24f) { update(patch.copy(vocoderShift = it)) }
        Knob("Speed", patch.vocoderSpeed, 0.01f..1f) { update(patch.copy(vocoderSpeed = it)) }
        Knob("Q", patch.vocoderQ, 0.5f..12f) { update(patch.copy(vocoderQ = it)) }
        Knob("Mix", patch.vocoderMix, 0f..1f) { update(patch.copy(vocoderMix = it)) }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Destruction Lab", style = MaterialTheme.typography.titleMedium)
        val destModes = listOf("Bypass", "Hard clip", "Assimétrico", "Wavefold", "Bitcrush", "Gate")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            destModes.forEachIndexed { i, name ->
                FilterChip(
                    selected = patch.destructionMode == i,
                    onClick = { update(patch.copy(destructionMode = i)) },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
        Knob("Drive", patch.destructionDrive, 1f..16f) { update(patch.copy(destructionDrive = it)) }
        Knob("Wet", patch.destructionWet, 0f..1f) { update(patch.copy(destructionWet = it)) }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Mod Matrix / LFO", style = MaterialTheme.typography.titleMedium)
        Row {
            Switch(
                checked = patch.modEnabled,
                onCheckedChange = { update(patch.copy(modEnabled = it)) },
            )
            Text("LFO ativo", style = MaterialTheme.typography.bodySmall)
        }
        val shapes = listOf("Sine", "Tri", "Square", "S&H")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            shapes.forEachIndexed { i, name ->
                FilterChip(
                    selected = patch.modShape == i,
                    onClick = { update(patch.copy(modShape = i)) },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
        val destinations = listOf("Ruído", "Sub", "Synth", "Mic")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            destinations.forEachIndexed { i, name ->
                FilterChip(
                    selected = patch.modDestination == i,
                    onClick = { update(patch.copy(modDestination = i)) },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
        Knob("Rate (Hz)", patch.modRate, 0.05f..20f) { update(patch.copy(modRate = it)) }
        Knob("Depth", patch.modDepth, 0f..1f) { update(patch.copy(modDepth = it)) }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Sub Bass + Synth", style = MaterialTheme.typography.titleMedium)
        val waves = listOf("Sine", "Tri", "Square", "Saw")
        Knob("Sub (Hz)", patch.subFrequency, 10f..200f) { update(patch.copy(subFrequency = it)) }
        Knob("Sub level", patch.subLevel, 0f..0.4f) { update(patch.copy(subLevel = it)) }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            waves.forEachIndexed { i, name ->
                FilterChip(
                    selected = patch.subWave == i,
                    onClick = { update(patch.copy(subWave = i)) },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
        Knob("Synth (Hz)", patch.synthFrequency, 30f..1200f) { update(patch.copy(synthFrequency = it)) }
        Knob("Synth level", patch.synthLevel, 0f..0.3f) { update(patch.copy(synthLevel = it)) }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            waves.forEachIndexed { i, name ->
                FilterChip(
                    selected = patch.synthWave == i,
                    onClick = { update(patch.copy(synthWave = i)) },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Master", style = MaterialTheme.typography.titleMedium)
        Knob("Threshold (dB)", patch.limiterThreshold, -24f..0f) { update(patch.copy(limiterThreshold = it)) }
        Knob("Volume", patch.masterGain, 0f..1f) { update(patch.copy(masterGain = it)) }
        Button(onClick = {
            update(patch.copy(masterGain = 0f, micGain = 0f, modDepth = 0f, destructionWet = 0f))
        }) { Text("KILL / SILÊNCIO") }
        Text("Meter L/R: ${controller.meter().contentToString()}")
    }
}
