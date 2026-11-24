package com.extremeaudiolab.audio

import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Receptor do stream P2P. Usa o mesmo APK em modo Receiver.
 * Cabeçalho: magic(Int) seq(Int) timestamp(Long) frames(Int) channels(Int) rate(Int) = 28 bytes.
 */
class StreamReceiver(private val port: Int) {
    @Volatile private var running = false
    private var socket: DatagramSocket? = null
    private var track: AudioTrack? = null

    suspend fun listen(onStatus: (String) -> Unit) = withContext(Dispatchers.IO) {
        socket = DatagramSocket(port).also { it.soTimeout = 1500 }
        var configured = false
        val buf = ByteArray(4096)
        running = true
        onStatus("Aguardando pacotes na porta $port…")
        while (running) {
            try {
                val packet = DatagramPacket(buf, buf.size)
                socket?.receive(packet)
                if (packet.length <= HEADER) continue
                val bb = ByteBuffer.wrap(packet.data, 0, packet.length).order(ByteOrder.LITTLE_ENDIAN)
                if (bb.int != MAGIC) continue
                bb.int // seq
                bb.long // timestamp
                val frames = bb.int
                val channels = bb.int
                val rate = bb.int
                if (!configured) {
                    track = AudioTrack.Builder()
                        .setAudioAttributes(
                            android.media.AudioAttributes.Builder()
                                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build(),
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(rate)
                                .setChannelMask(if (channels == 2) AudioFormat.CHANNEL_OUT_STEREO else AudioFormat.CHANNEL_OUT_MONO)
                                .build(),
                        )
                        .setBufferSizeInBytes(frames * channels * 2 * 4)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                        .also { it.play() }
                    configured = true
                    onStatus("Conectado: ${rate}Hz / ${channels}ch")
                }
                val pcm = ByteArray(frames * channels * 2)
                System.arraycopy(packet.data, HEADER, pcm, 0, pcm.size)
                track?.write(pcm, 0, pcm.size)
            } catch (e: java.net.SocketTimeoutException) {
                // continua
            } catch (e: Exception) {
                if (running) onStatus("Erro: ${e.message}")
            }
        }
    }

    fun stop() {
        running = false
        socket?.close()
        track?.stop()
        track?.release()
        socket = null
        track = null
    }

    private companion object {
        const val MAGIC = 0x484E4C31 // 'HNL1'
        const val HEADER = 28
    }
}
