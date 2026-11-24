package com.extremeaudiolab.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extremeaudiolab.model.Patch

private data class KaossProgram(val name: String, val hint: String)

private val programs = listOf(
    KaossProgram("Filtro + Eco", "Esquerda/direita abre o filtro. Em cima aumenta o delay."),
    KaossProgram("Phaser + Reverb", "Esquerda/direita controla a velocidade; em cima o tamanho do reverb."),
    KaossProgram("Flanger + Slicer", "Esquerda/direita abre o flanger; em cima corta o som em fatias."),
    KaossProgram("Chorus + Delay", "Esquerda/direita controla o chorus; em cima o eco."),
    KaossProgram("Ring Mod", "Esquerda/direita muda a frequência metálica; em cima o filtro."),
    KaossProgram("Pitch + Reverb", "Esquerda/direita muda o efeito de altura; em cima o reverb."),
    KaossProgram("Tremolo + Dist", "Esquerda/direita acelera o tremolo; em cima a distorção."),
    KaossProgram("Vinil + Gate", "Esquerda/direita controla estalos e cortes; em baixo fica mais limpo."),
)

@Composable
fun KaossScreen(patch: Patch, update: (Patch) -> Unit, controller: AudioController) {
    val k = patch.kaoss

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        SectionCard(
            title = "Kaoss Pad — ${programs[k.program].name}",
            subtitle = programs[k.program].hint,
            icon = "☯",
        ) {
            Text(
                "Arraste o dedo pela área abaixo. Para a esquerda/em baixo = menos efeito; para a direita/em cima = mais.",
                style = MaterialTheme.typography.bodySmall,
                color = LabColors.TextMuted,
            )
            Spacer(Modifier.height(8.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LabColors.Surface2)
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
                Canvas(Modifier.matchParentSize()) {
                    val line = LabColors.Track.copy(alpha = 0.7f)
                    for (i in 1..7) {
                        val gx = size.width * i / 8f
                        val gy = size.height * i / 8f
                        drawLine(line, Offset(gx, 0f), Offset(gx, size.height), 1.dp.toPx())
                        drawLine(line, Offset(0f, gy), Offset(size.width, gy), 1.dp.toPx())
                    }
                    val cx = size.width * k.x
                    val cy = size.height * k.y
                    drawLine(
                        LabColors.Accent.copy(alpha = 0.35f),
                        Offset(cx, 0f), Offset(cx, size.height), 1.5.dp.toPx(),
                    )
                    drawLine(
                        LabColors.Accent.copy(alpha = 0.35f),
                        Offset(0f, cy), Offset(size.width, cy), 1.5.dp.toPx(),
                    )
                    drawCircle(LabColors.Accent.copy(alpha = 0.18f), 26.dp.toPx(), Offset(cx, cy))
                    drawCircle(LabColors.Accent, 9.dp.toPx(), Offset(cx, cy))
                }
                Text(
                    "EM CIMA = MAIS",
                    color = LabColors.TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                )
                Text(
                    "EM BAIXO = MENOS",
                    color = LabColors.TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                )
                Text(
                    "X ${(k.x * 100).toInt()}%   Y ${(k.y * 100).toInt()}%",
                    color = LabColors.Text,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(top = 34.dp),
                )
            }

            Spacer(Modifier.height(10.dp))
            Text("Escolha o efeito:", style = MaterialTheme.typography.labelLarge, color = LabColors.Text)
            Spacer(Modifier.height(4.dp))
            Segmented(programs.map { it.name }, k.program) { i ->
                val next = k.copy(program = i)
                update(patch.copy(kaoss = next))
                controller.setKaoss(next.program, next.x, next.y, next.latch, next.mix)
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = k.latch, onCheckedChange = {
                    val next = k.copy(latch = it)
                    update(patch.copy(kaoss = next))
                    controller.setKaoss(next.program, next.x, next.y, next.latch, next.mix)
                })
                Text(" Travar posição", color = LabColors.Text)
            }
            Text(
                "Travar mantém o ponto escolhido mesmo se você tirar o dedo.",
                style = MaterialTheme.typography.bodySmall,
                color = LabColors.TextMuted,
            )
            Fader(
                "Força do efeito", k.mix, 0f..1f,
                {
                    val next = k.copy(mix = it)
                    update(patch.copy(kaoss = next))
                    controller.setKaoss(next.program, next.x, next.y, next.latch, next.mix)
                },
                description = "Quanto do efeito Kaoss entra na mistura. Em 0 o pad fica desligado.",
            )
        }
    }
}
