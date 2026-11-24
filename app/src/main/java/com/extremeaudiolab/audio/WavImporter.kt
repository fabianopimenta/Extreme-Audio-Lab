package com.extremeaudiolab.audio

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Leitor de WAV local (PCM 8/16/24/32 bits e float 32/64).
 *
 * O arquivo vem do armazenamento do próprio aparelho, pelo seletor do Android.
 * Nada é enviado para a internet: o áudio só existe na memória do app.
 * A amostra é limitada a 20 s para não estourar a memória do celular.
 */
object WavImporter {
    data class Loaded(val samples: FloatArray, val sampleRate: Int, val seconds: Float)

    private const val MAX_SECONDS = 20f
    private const val MAX_BYTES = 24 * 1024 * 1024

    fun load(context: Context, uri: Uri): Loaded? {
        val bytes = context.contentResolver.openInputStream(uri)?.use { readLimited(it) } ?: return null
        return decode(bytes)
    }

    /** Lê uma textura WAV que acompanha o APK (assets/samples). */
    fun loadAsset(context: Context, assetPath: String): Loaded? {
        val bytes = context.assets.open(assetPath).use { readLimited(it) }
        return decode(bytes)
    }

    private fun readLimited(input: InputStream): ByteArray {
        val out = ByteArrayOutputStream(1 shl 20)
        val buf = ByteArray(64 * 1024)
        var total = 0
        while (true) {
            val n = input.read(buf)
            if (n <= 0) break
            val take = minOf(n, MAX_BYTES - total)
            if (take <= 0) break
            out.write(buf, 0, take)
            total += take
            if (total >= MAX_BYTES) break
        }
        return out.toByteArray()
    }

    private fun u8(b: ByteArray, i: Int): Int = b[i].toInt() and 0xFF
    private fun u16(b: ByteArray, i: Int): Int = u8(b, i) or (u8(b, i + 1) shl 8)
    private fun i32(b: ByteArray, i: Int): Int =
        u8(b, i) or (u8(b, i + 1) shl 8) or (u8(b, i + 2) shl 16) or (u8(b, i + 3) shl 24)

    private fun tag(b: ByteArray, i: Int): String =
        if (i + 4 > b.size) "" else String(b, i, 4, Charsets.US_ASCII)

    fun decode(b: ByteArray): Loaded? {
        if (b.size < 44) return null
        if (tag(b, 0) != "RIFF" || tag(b, 8) != "WAVE") return null

        var fmtFormat = 0
        var channels = 0
        var sampleRate = 0
        var bits = 0
        var dataOffset = -1
        var dataSize = 0

        var pos = 12
        while (pos + 8 <= b.size) {
            val id = tag(b, pos)
            var size = i32(b, pos + 4)
            if (size < 0) size = 0
            val body = pos + 8
            if (body + size > b.size) size = b.size - body          // arquivo truncado
            when (id) {
                "fmt " -> {
                    if (size >= 16) {
                        fmtFormat = u16(b, body)
                        channels = u16(b, body + 2)
                        sampleRate = i32(b, body + 4)
                        bits = u16(b, body + 14)
                    }
                    if (fmtFormat == 0xFFFE && size >= 26) {
                        // WAVE_FORMAT_EXTENSIBLE: o formato real está no GUID.
                        fmtFormat = u16(b, body + 24)
                    }
                }
                "data" -> {
                    dataOffset = body
                    dataSize = size
                }
            }
            pos = body + size + (size and 1)                       // chunks são alinhados em 2
            if (dataOffset >= 0 && fmtFormat != 0) break
        }

        if (dataOffset < 0 || dataSize <= 0) return null
        if (channels <= 0 || channels > 8) return null
        if (sampleRate < 8_000 || sampleRate > 384_000) return null
        val isFloat = fmtFormat == 3
        if (fmtFormat != 1 && !isFloat) return null
        if (bits != 8 && bits != 16 && bits != 24 && bits != 32 && !(isFloat && bits == 64)) return null

        val bytesPerSample = bits / 8
        val frameBytes = bytesPerSample * channels
        if (frameBytes <= 0) return null
        val totalFrames = dataSize / frameBytes
        if (totalFrames <= 0) return null
        val maxFrames = minOf(totalFrames, (sampleRate * MAX_SECONDS).toInt())
        val out = FloatArray(maxFrames)
        val scale = 1f / channels

        for (f in 0 until maxFrames) {
            var acc = 0f
            val base = dataOffset + f * frameBytes
            for (c in 0 until channels) {
                val i = base + c * bytesPerSample
                val v = when {
                    isFloat && bits == 32 -> Float.fromBits(i32(b, i))
                    isFloat && bits == 64 -> {
                        val lo = i32(b, i).toLong() and 0xFFFFFFFFL
                        val hi = i32(b, i + 4).toLong()
                        Double.fromBits((hi shl 32) or lo).toFloat()
                    }
                    bits == 8 -> (u8(b, i) - 128) / 128f
                    bits == 16 -> {
                        val s = (u16(b, i).toShort()).toInt()
                        s / 32768f
                    }
                    bits == 24 -> {
                        var s = u8(b, i) or (u8(b, i + 1) shl 8) or (u8(b, i + 2) shl 16)
                        if ((s and 0x800000) != 0) s = s or 0xFF000000.toInt()
                        s / 8388608f
                    }
                    else -> i32(b, i) / 2147483648f
                }
                acc += if (v.isFinite()) v else 0f
            }
            out[f] = (acc * scale).coerceIn(-1f, 1f)
        }
        if (out.isEmpty()) return null
        return Loaded(out, sampleRate, out.size.toFloat() / sampleRate.toFloat())
    }
}
