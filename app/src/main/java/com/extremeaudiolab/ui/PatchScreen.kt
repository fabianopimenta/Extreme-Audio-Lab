package com.extremeaudiolab.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

        SectionCard("Preset — ponto de partida", "Escolha um som pronto e depois ajuste.", "★") {
            ActionButton(text = "Presets (${presets.size})  ▾", onClick = { expanded = true })
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
                Text(
                    current.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = LabColors.TextMuted,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        SectionCard("Dinâmica — o que faz o som 'viver'", "Os 3 faders mais importantes desta tela.", "〰") {
            Fader(
                "Movimento", patch.motionDepth, 0f..1f, { update(patch.copy(motionDepth = it)) },
                description = "Faz o volume e o brilho subirem e descerem devagar, como uma respiração. Em 0 o som fica parado; em 0,3 ele já ganha vida.",
            )
            Fader(
                "Velocidade do movimento", patch.motionRate, 0.05f..4f,
                { update(patch.copy(motionRate = it)) },
                curve = FaderCurve.LOG, unit = " Hz",
                description = "Quantas 'respirações' por segundo. 0,2 Hz é lento e dramático; 2 Hz vira um tremolo rápido.",
            )
            Fader(
                "Saturação final", patch.saturation, 0f..1f, { update(patch.copy(saturation = it)) },
                description = "Dá 'dente' e sujeira no resultado final, sem mudar o volume. 0 = mais limpo, 1 = bem agressivo.",
            )
            Fader(
                "Teto de segurança", patch.limiterThreshold, -12f..0f,
                { update(patch.copy(limiterThreshold = it)) },
                unit = " dB",
                description = "Só corta o som quando ele passa deste limite. -2 dB evita distorção de volume sem achatar os outros faders.",
            )
            Fader(
                "Volume geral", patch.masterGain, 0f..1f, { update(patch.copy(masterGain = it)) },
                description = "Volume final do app. Agora ele age antes do teto de segurança, então mover este fader muda o nível de verdade.",
            )
            ActionButton(
                text = "KILL / SILÊNCIO",
                onClick = {
                    update(patch.copy(masterGain = 0f, micGain = 0f, modDepth = 0f, destructionWet = 0f))
                },
                danger = true,
            )
        }

        SectionCard("Fonte de ruído", "A 'matéria-prima' áspera do laboratório.", "≋") {
            Fader(
                "Nível do ruído", patch.noiseLevel, 0f..1f, { update(patch.copy(noiseLevel = it)) },
                description = "Quanto de chiado entra na mistura. É o fader que mais muda o som nesta seção: 0 = silêncio, 1 = barulho total.",
            )
            Fader(
                "Qualidade (bits)", patch.noiseCrush, 2f..16f, { update(patch.copy(noiseCrush = it)) },
                description = "Simula áudio de baixa qualidade. Quanto menor o número, mais 'quebrado' e digital fica o som; 16 = sem redução.",
            )
            Fader(
                "Congelamento (Hz)", patch.noiseHold, 1f..8000f, { update(patch.copy(noiseHold = it)) },
                curve = FaderCurve.LOG, unit = " Hz",
                description = "Segura cada amostra por um instante. Valores baixos (20–200) dão um ronronar áspero; altos viram chiado fino.",
            )
            Fader(
                "Eco metálico", patch.noiseComb, 0f..1f, { update(patch.copy(noiseComb = it)) },
                description = "Soma cópias deslocadas do ruído, criando um som de tubo/metal. Aumente devagar.",
            )
            Fader(
                "Tipo de ruído", patch.noiseType.toFloat(), 0f..4f,
                { update(patch.copy(noiseType = it.toInt())) },
                description = "0 branco, 1 rosa (mais grave), 2 marrom (muito grave), 3 estalos, 4 amostragem. Use os botões abaixo do fader para trocar.",
            )
        }

        SectionCard("Microfone / Feedback", "Fale ou aproxime o celular da caixa de som.", "🎤") {
            Fader(
                "Ganho do microfone", patch.micGain, 0f..0.95f, { update(patch.copy(micGain = it)) },
                description = "Quanto o som captado é realimentado. Atenção: quanto maior, maior o risco de apito. Nunca comece em 0,9.",
            )
            Fader(
                "Delay do loop", patch.micDelayMs, 1f..200f, { update(patch.copy(micDelayMs = it)) },
                curve = FaderCurve.LOG, unit = " ms",
                description = "Distância entre uma repetição e outra do som. Pequeno (5–20) vira apito; grande (100+) vira eco.",
            )
            Fader(
                "Cor do feedback", patch.micTone, 100f..8000f, { update(patch.copy(micTone = it)) },
                curve = FaderCurve.LOG, unit = " Hz",
                description = "Escolhe qual faixa de frequência vai realçar. Baixo = grave/ronco; alto = agudo/apito.",
            )
            Fader(
                "Mix do microfone", patch.micMix, 0f..1f, { update(patch.copy(micMix = it)) },
                description = "Quanto do microfone aparece na mistura. Se você não fala nem usa caixa, deixe em 0 — este fader não fará nada.",
            )
            Fader(
                "Porta de ruído", patch.micGate, 0f..1f, { update(patch.copy(micGate = it)) },
                description = "Corta sons muito baixos para reduzir chiado de fundo. 0,08 costuma ser suficiente.",
            )
        }

        SectionCard("Hyper Fuzz", "Distorção suja com oitava e 'engasgo'.", "⚡") {
            Fader("Drive", patch.fuzzDrive, 0f..1f, { update(patch.copy(fuzzDrive = it)) },
                description = "Quantidade de distorção. Entre 0,5 e 0,9 a mudança é bem audível.")
            Fader("Oitava", patch.fuzzOctave, 0f..1f, { update(patch.copy(fuzzOctave = it)) },
                description = "Mistura uma versão uma oitava acima, deixando o som mais 'metálico' e estridente.")
            Fader("Gate", patch.fuzzGate, 0f..1f, { update(patch.copy(fuzzGate = it)) },
                description = "Corta o som quando ele fica fraco, criando engasgos rítmicos. 0 = sem corte.")
            Fader("Tone", patch.fuzzTone, 0f..1f, { update(patch.copy(fuzzTone = it)) },
                description = "Brilho da distorção: 0 = escuro e abafado, 1 = bem agudo.")
        }

        SectionCard("DOD Death Metal", "A 'caixa de guitarra' aggressiva da cadeia.", "🔥") {
            Fader("Gain", patch.dodGain, 0f..1f, { update(patch.copy(dodGain = it)) },
                description = "Ganho do pedal. Quanto maior, mais compacto e distorcido fica o som.")
            Fader("Corte de graves", patch.dodLowCut, 0f..1f, { update(patch.copy(dodLowCut = it)) },
                description = "Remove frequências graves para desafogar a mistura.")
            Fader("Scoop de médios", patch.dodScoop, 0f..1f, { update(patch.copy(dodScoop = it)) },
                description = "Tira médios e cria aquele som 'furado' típico de death metal.")
            Fader("Presença", patch.dodPresence, 0f..1f, { update(patch.copy(dodPresence = it)) },
                description = "Realça agudos para dar corte e definição.")
            Fader("Gate", patch.dodGate, 0f..1f, { update(patch.copy(dodGate = it)) },
                description = "Silencia trechos fracos, deixando a distorção mais seca e staccato.")
        }

        SectionCard("Glitch / Breakdown", "Trava, inverte e congela o som.", "⚙") {
            Segmented(listOf("Stutter", "Reverse", "Freeze", "Decimate"), patch.glitchMode) {
                update(patch.copy(glitchMode = it))
            }
            Fader("Velocidade do clock", patch.glitchRate, 0.5f..40f,
                { update(patch.copy(glitchRate = it)) }, curve = FaderCurve.LOG, unit = " Hz",
                description = "Quantas vezes por segundo o efeito tenta acontecer.")
            Fader("Tamanho do corte", patch.glitchSize, 10f..400f,
                { update(patch.copy(glitchSize = it)) }, curve = FaderCurve.LOG, unit = " ms",
                description = "Duração de cada pedacinho repetido. Cortes curtos engasgam; longos viram loop.")
            Fader("Chance", patch.glitchProbability, 0f..1f,
                { update(patch.copy(glitchProbability = it)) },
                description = "Probabilidade do glitch disparar a cada clock. 0 = nunca, 1 = sempre.")
            Fader("Redução", patch.glitchDecimate, 1f..64f,
                { update(patch.copy(glitchDecimate = it)) }, curve = FaderCurve.LOG, unit = "x",
                description = "No modo Decimate: quantas amostras são mantidas antes de trocar. Quanto maior, mais 'quebrado'.")
            Fader("Bits", patch.glitchBits, 2f..16f, { update(patch.copy(glitchBits = it)) },
                description = "No modo Decimate: resolução. 4 bits soa bem digital; 16 soa limpo.")
            Fader("Mix", patch.glitchMix, 0f..1f, { update(patch.copy(glitchMix = it)) },
                description = "Quanto do efeito entra na mistura. Se estiver em 0, os outros faders desta seção não aparecem.")
            Fader("Queda no break", patch.glitchGate, 0f..1f, { update(patch.copy(glitchGate = it)) },
                description = "Diminui o volume geral durante o glitch, criando a sensação de 'quebra'.")
            ActionButton("Disparar glitch agora", onClick = { controller.triggerGlitch() })
        }

        SectionCard("Ressonador Modal", "Sinos e metais que respondem a impactos.", "🔔") {
            Fader("Afinação", patch.modalTune, 40f..2000f, { update(patch.copy(modalTune = it)) },
                curve = FaderCurve.LOG, unit = " Hz",
                description = "Nota base do metal. Baixo = sino grave; alto = ferro agudo.")
            Fader("Inarmonicidade", patch.modalInharmonicity, 0f..1f,
                { update(patch.copy(modalInharmonicity = it)) },
                description = "Quão 'não musical' são os parciais. Alto soa mais como sucata do que como sino.")
            Fader("Decaimento", patch.modalDecay, 0f..1f, { update(patch.copy(modalDecay = it)) },
                description = "Quanto tempo o som do metal demora para sumir.")
            Fader("Brilho", patch.modalBrightness, 0f..1f, { update(patch.copy(modalBrightness = it)) },
                description = "Quantos agudos o ressonador deixa passar.")
            Fader("Mix", patch.modalMix, 0f..1f, { update(patch.copy(modalMix = it)) },
                description = "Presença do ressonador. Comece em 0,2–0,4 para ouvir sem dominar.")
        }

        SectionCard("Granular", "Pedaços de som espalhados, congelados ou invertidos.", "✦") {
            Segmented(listOf("Scatter", "Freeze", "Reverse", "PitchDrop"), patch.granularMode) {
                update(patch.copy(granularMode = it))
            }
            Fader("Tamanho do grão", patch.granularSize, 20f..800f,
                { update(patch.copy(granularSize = it)) }, curve = FaderCurve.LOG, unit = " ms",
                description = "Duração de cada fragmento. Pequeno = textura; grande = borrão sonoro.")
            Fader("Densidade", patch.granularDensity, 1f..80f,
                { update(patch.copy(granularDensity = it)) }, curve = FaderCurve.LOG, unit = "/s",
                description = "Quantos fragmentos por segundo. Mais densidade = som mais contínuo.")
            Fader("Pitch", patch.granularPitch, -24f..24f, { update(patch.copy(granularPitch = it)) },
                unit = " st", description = "Sobe ou desce a altura dos fragmentos, em semitons.")
            Fader("Dispersão", patch.granularSpread, 0f..1f, { update(patch.copy(granularSpread = it)) },
                description = "Quão longe no passado os fragmentos são buscados.")
            Fader("Feedback", patch.granularFeedback, 0f..0.85f,
                { update(patch.copy(granularFeedback = it)) },
                description = "Devolve parte do resultado para o histórico, criando camadas.")
            Fader("Mix", patch.granularMix, 0f..1f, { update(patch.copy(granularMix = it)) },
                description = "Presença do granular. Deixe acima de 0 para ouvir os efeitos desta seção.")
            ActionButton("Disparar grãos", onClick = { controller.triggerGranular() })
        }

        SectionCard("Vocoder / Formantes", "Faz o som 'falar' com o microfone.", "🗣") {
            Segmented(listOf("Ruído", "Osc", "Ambos"), patch.vocoderSource) {
                update(patch.copy(vocoderSource = it))
            }
            Fader("Portadora", patch.vocoderPitch, 40f..1200f,
                { update(patch.copy(vocoderPitch = it)) }, curve = FaderCurve.LOG, unit = " Hz",
                description = "Frequência da voz artificial quando a fonte é Osc ou Ambos.")
            Fader("Deslocamento", patch.vocoderShift, -24f..24f,
                { update(patch.copy(vocoderShift = it)) }, unit = " st",
                description = "Move as formantes para cima ou para baixo, mudando o 'timbre' da voz.")
            Fader("Velocidade", patch.vocoderSpeed, 0.01f..1f,
                { update(patch.copy(vocoderSpeed = it)) }, curve = FaderCurve.LOG,
                description = "Quanto as formantes se movem ao longo do tempo.")
            Fader("Q", patch.vocoderQ, 0.5f..12f, { update(patch.copy(vocoderQ = it)) },
                description = "Nitidez das bandas. Alto soa mais 'robótico' e afinado.")
            Fader("Mix", patch.vocoderMix, 0f..1f, { update(patch.copy(vocoderMix = it)) },
                description = "Quanto da voz artificial entra. Precisa de microfone para ser notado.")
        }

        SectionCard("Destruction Lab", " seis formas de quebrar o sinal.", "💥") {
            Segmented(
                listOf("Bypass", "Hard clip", "Assimétrico", "Wavefold", "Bitcrush", "Gate"),
                patch.destructionMode,
            ) { update(patch.copy(destructionMode = it)) }
            Fader("Drive", patch.destructionDrive, 1f..16f,
                { update(patch.copy(destructionDrive = it)) }, curve = FaderCurve.LOG, unit = "x",
                description = "Quanto o sinal é empurrado contra o algoritmo de destruição.")
            Fader("Wet", patch.destructionWet, 0f..1f, { update(patch.copy(destructionWet = it)) },
                description = "Quantidade do efeito. Em 0 o módulo está desligado, mesmo com outro modo escolhido.")
        }

        SectionCard("Mod Matrix / LFO", "Movimento automático em uma parte do som.", "〽") {
            Row {
                androidx.compose.material3.Switch(
                    checked = patch.modEnabled,
                    onCheckedChange = { update(patch.copy(modEnabled = it)) },
                )
                Text(" LFO ativo", color = LabColors.Text, modifier = Modifier.padding(start = 6.dp))
            }
            Segmented(listOf("Seno", "Triângulo", "Quadrada", "Aleatório"), patch.modShape) {
                update(patch.copy(modShape = it))
            }
            Segmented(listOf("Ruído", "Sub", "Synth", "Mic"), patch.modDestination) {
                update(patch.copy(modDestination = it))
            }
            Fader("Velocidade do LFO", patch.modRate, 0.05f..20f,
                { update(patch.copy(modRate = it)) }, curve = FaderCurve.LOG, unit = " Hz",
                description = "Quantas oscilações por segundo do movimento automático.")
            Fader("Profundidade", patch.modDepth, 0f..1f, { update(patch.copy(modDepth = it)) },
                description = "Quanto o LFO mexe no destino escolhido. 0 = ligado mas sem efeito.")
        }

        SectionCard("Sub Bass + Synth", "Duas vozes musicais para dar corpo.", "🎹") {
            Fader("Sub — frequência", patch.subFrequency, 10f..200f,
                { update(patch.copy(subFrequency = it)) }, curve = FaderCurve.LOG, unit = " Hz",
                description = "Nota do grave. 40–60 Hz é 'sub bomb'; acima disso vira baixo audível.")
            Fader("Sub — nível", patch.subLevel, 0f..0.4f, { update(patch.copy(subLevel = it)) },
                description = "Volume do grave. Se estiver em 0, mudar a frequência não produz som.")
            Segmented(listOf("Seno", "Tri", "Quad", "Serra"), patch.subWave) {
                update(patch.copy(subWave = it))
            }
            Fader("Synth — frequência", patch.synthFrequency, 30f..1200f,
                { update(patch.copy(synthFrequency = it)) }, curve = FaderCurve.LOG, unit = " Hz",
                description = "Nota da voz aguda.")
            Fader("Synth — nível", patch.synthLevel, 0f..0.3f, { update(patch.copy(synthLevel = it)) },
                description = "Volume da voz aguda. Aumente para ouvir a diferença entre as formas de onda.")
            Segmented(listOf("Seno", "Tri", "Quad", "Serra"), patch.synthWave) {
                update(patch.copy(synthWave = it))
            }
        }
    }
}
