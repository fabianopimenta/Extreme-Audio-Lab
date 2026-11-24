package com.extremeaudiolab.ui

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val dir = File(ctx.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "ExtremeAudioLab").also { it.mkdirs() }
    var files by remember { mutableStateOf(dir.listFiles()?.toList().orEmpty()) }
    var recording by remember { mutableStateOf(false) }
    var host by remember { mutableStateOf("192.168.43.1") }
    var port by remember { mutableStateOf("5004") }
    var streaming by remember { mutableStateOf(false) }
    var receiverMode by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Pronto") }
    var receiver by remember { mutableStateOf<StreamReceiver?>(null) }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Gravação", style = MaterialTheme.typography.titleMedium)
        Button(onClick = {
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
        }) { Text(if (recording) "Parar gravação" else "Gravar WAV") }

        LazyColumn(Modifier.weight(1f)) {
            items(files) { f ->
                Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(f.name, style = MaterialTheme.typography.labelSmall)
                    Row {
                        Button(onClick = {
                            val uri = Recorder.publish(ctx, f)
                            if (uri != null) ctx.startActivity(Recorder.shareIntent(uri))
                            else status = "Falha ao publicar"
                        }) { Text("Baixar/Compartilhar") }
                    }
                }
            }
        }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Transmissão P2P (offline)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(host, { host = it }, label = { Text("IP do receptor") }, singleLine = true)
        OutlinedTextField(port, { port = it }, label = { Text("Porta UDP") }, singleLine = true)
        Row {
            Button(onClick = {
                val p = port.toIntOrNull() ?: 5004
                streaming = controller.setStream(host, p)
                status = if (streaming) "Transmitindo para $host:$p" else "Falha ao iniciar stream"
            }, enabled = !streaming) { Text("Start stream") }
            Button(onClick = { controller.stopStream(); streaming = false; status = "Stream parado" }, enabled = streaming) {
                Text("Stop")
            }
        }

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Switch(checked = receiverMode, onCheckedChange = { on ->
                receiverMode = on
                if (on) {
                    val r = StreamReceiver(port.toIntOrNull() ?: 5004)
                    receiver = r
                    scope.launch { r.listen { status = it } }
                } else {
                    receiver?.stop(); receiver = null
                }
            })
            Text("Modo Receiver neste dispositivo")
        }
        Text(status, style = MaterialTheme.typography.bodySmall)
    }
}

private fun Context.noop() = Unit
