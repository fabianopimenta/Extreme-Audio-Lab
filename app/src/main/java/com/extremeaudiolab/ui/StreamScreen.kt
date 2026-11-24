package com.extremeaudiolab.ui

import android.os.Environment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.extremeaudiolab.audio.Recorder
import com.extremeaudiolab.audio.StreamReceiver
import com.extremeaudiolab.model.Patch
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun StreamScreen(patch: Patch, controller: AudioController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val dir = File(ctx.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "PMNTNoiseLab").also { it.mkdirs() }
    var files by remember { mutableStateOf(dir.listFiles()?.toList().orEmpty()) }
    var recording by remember { mutableStateOf(false) }
    var host by remember { mutableStateOf("192.168.43.1") }
    var port by remember { mutableStateOf("5004") }
    var streaming by remember { mutableStateOf(false) }
    var receiverMode by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Pronto") }
    var receiver by remember { mutableStateOf<StreamReceiver?>(null) }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        SectionCard("Gravação WAV", "Salva o áudio no celular, sem internet.", "⏺", EuroColors.Buchla) {
            ActionButton(
                text = if (recording) "Parar gravação" else "Gravar WAV",
                onClick = {
                    if (!recording) {
                        val f = File(dir, "eal_${System.currentTimeMillis()}.wav")
                        recording = controller.startRecording(f.absolutePath)
                        status = if (recording) "Gravando…" else "Falha ao iniciar gravação"
                    } else {
                        controller.stopRecording()
                        recording = false
                        files = dir.listFiles()?.toList().orEmpty()
                        status = "Gravação salva"
                    }
                },
                danger = recording,
            )
            Text(
                "Arquivos salvos em Música/ExtremeAudioLab. Toque em Compartilhar para enviar ou salvar em outro lugar.",
                style = MaterialTheme.typography.bodySmall,
                color = LabColors.TextMuted,
            )
            LazyColumn(Modifier.weight(1f).height(140.dp)) {
                items(files) { f ->
                    Row(
                        Modifier.fillMaxWidth().padding(4.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(f.name, style = MaterialTheme.typography.labelSmall, color = LabColors.Text)
                        ActionButton(
                            text = "Compartilhar",
                            onClick = {
                                val uri = Recorder.publish(ctx, f)
                                if (uri != null) ctx.startActivity(Recorder.shareIntent(uri))
                                else status = "Falha ao publicar"
                            },
                        )
                    }
                }
            }
        }

        SectionCard(
            "Stream P2P — som direto entre celulares",
            "Funciona na mesma rede Wi-Fi, sem servidor.",
            "📡",
            EuroColors.Erica,
        ) {
            Text(
                "No celular que vai receber, ligue o Modo Receiver. No celular que vai enviar, digite o IP mostrado na rede e a mesma porta.",
                style = MaterialTheme.typography.bodySmall,
                color = LabColors.TextMuted,
            )
            OutlinedTextField(
                host, { host = it },
                label = { Text("IP do receptor") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                port, { port = it },
                label = { Text("Porta UDP") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
            Row {
                ActionButton(
                    text = "Enviar áudio",
                    enabled = !streaming,
                    onClick = {
                        val p = port.toIntOrNull() ?: 5004
                        streaming = controller.setStream(host, p)
                        status = if (streaming) "Transmitindo para $host:$p" else "Falha ao iniciar envio"
                    },
                )
                Spacer(Modifier.padding(horizontal = 4.dp))
                ActionButton(
                    text = "Parar envio",
                    enabled = streaming,
                    onClick = {
                        controller.stopStream()
                        streaming = false
                        status = "Envio parado"
                    },
                    danger = true,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = receiverMode,
                    onCheckedChange = { on ->
                        receiverMode = on
                        if (on) {
                            val r = StreamReceiver(port.toIntOrNull() ?: 5004)
                            receiver = r
                            scope.launch { r.listen { status = it } }
                        } else {
                            receiver?.stop()
                            receiver = null
                        }
                    },
                )
                Text(" Modo Receiver neste dispositivo", color = LabColors.Text)
            }
            Text(status, style = MaterialTheme.typography.bodySmall, color = LabColors.Accent)
        }
    }
}
