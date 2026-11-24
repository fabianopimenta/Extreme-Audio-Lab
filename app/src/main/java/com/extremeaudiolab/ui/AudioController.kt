package com.extremeaudiolab.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.extremeaudiolab.audio.AudioService
import com.extremeaudiolab.model.Patch

/** Faz o bind do serviço e espelha o Patch para os parâmetros nativos. */
class AudioController(private val context: Context) {
    private var service: AudioService? = null
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as? AudioService.LocalBinder)?.service()
            bound = true
            service?.start()
            // Se o usuário escolheu uma amostra antes do serviço subir, envia agora.
            flushSample()
            applySampleParams()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            bound = false
        }
    }

    fun bind() {
        val intent = Intent(context, AudioService::class.java)
        context.startForegroundService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbind() {
        if (bound) context.unbindService(connection)
        bound = false
        service = null
    }

    fun applyPatch(patch: Patch) {
        val s = service ?: return
        s.setParam("noise", "type", patch.noiseType.toFloat())
        s.setParam("noise", "level", patch.noiseLevel)
        s.setParam("noise", "crush", patch.noiseCrush)
        s.setParam("noise", "hold", patch.noiseHold)
        s.setParam("noise", "comb", patch.noiseComb)
        s.setParam("mic", "gain", patch.micGain)
        s.setParam("mic", "delayMs", patch.micDelayMs)
        s.setParam("mic", "tone", patch.micTone)
        s.setParam("mic", "mix", patch.micMix)
        s.setParam("mic", "gate", patch.micGate)
        s.setTempo(patch.bpm, patch.swing)
        patch.steps.forEachIndexed { i, st ->
            s.setStep(i, st.on, st.voice, st.pitch, st.velocity, st.probability)
        }
        s.setParam("fuzz", "drive", patch.fuzzDrive)
        s.setParam("fuzz", "octave", patch.fuzzOctave)
        s.setParam("fuzz", "gate", patch.fuzzGate)
        s.setParam("fuzz", "tone", patch.fuzzTone)
        s.setParam("dod", "gain", patch.dodGain)
        s.setParam("dod", "lowCut", patch.dodLowCut)
        s.setParam("dod", "scoop", patch.dodScoop)
        s.setParam("dod", "presence", patch.dodPresence)
        s.setParam("dod", "gate", patch.dodGate)
        s.setParam("glitch", "mode", patch.glitchMode.toFloat())
        s.setParam("glitch", "rate", patch.glitchRate)
        s.setParam("glitch", "size", patch.glitchSize)
        s.setParam("glitch", "decimate", patch.glitchDecimate)
        s.setParam("glitch", "bits", patch.glitchBits)
        s.setParam("glitch", "mix", patch.glitchMix)
        s.setParam("glitch", "probability", patch.glitchProbability)
        s.setParam("glitch", "gate", patch.glitchGate)
        s.setParam("modal", "tune", patch.modalTune)
        s.setParam("modal", "inharmonicity", patch.modalInharmonicity)
        s.setParam("modal", "decay", patch.modalDecay)
        s.setParam("modal", "brightness", patch.modalBrightness)
        s.setParam("modal", "mix", patch.modalMix)
        s.setParam("granular", "mode", patch.granularMode.toFloat())
        s.setParam("granular", "size", patch.granularSize)
        s.setParam("granular", "density", patch.granularDensity)
        s.setParam("granular", "pitch", patch.granularPitch)
        s.setParam("granular", "spread", patch.granularSpread)
        s.setParam("granular", "feedback", patch.granularFeedback)
        s.setParam("granular", "mix", patch.granularMix)
        s.setParam("vocoder", "source", patch.vocoderSource.toFloat())
        s.setParam("vocoder", "pitch", patch.vocoderPitch)
        s.setParam("vocoder", "shift", patch.vocoderShift)
        s.setParam("vocoder", "speed", patch.vocoderSpeed)
        s.setParam("vocoder", "q", patch.vocoderQ)
        s.setParam("vocoder", "mix", patch.vocoderMix)
        s.setParam("destruction", "mode", patch.destructionMode.toFloat())
        s.setParam("destruction", "drive", patch.destructionDrive)
        s.setParam("destruction", "wet", patch.destructionWet)
        s.setParam("mod", "enabled", if (patch.modEnabled) 1f else 0f)
        s.setParam("mod", "rate", patch.modRate)
        s.setParam("mod", "depth", patch.modDepth)
        s.setParam("mod", "destination", patch.modDestination.toFloat())
        s.setParam("mod", "shape", patch.modShape.toFloat())
        s.setParam("sub", "frequency", patch.subFrequency)
        s.setParam("sub", "level", patch.subLevel)
        s.setParam("sub", "wave", patch.subWave.toFloat())
        s.setParam("synth", "frequency", patch.synthFrequency)
        s.setParam("synth", "level", patch.synthLevel)
        s.setParam("synth", "wave", patch.synthWave.toFloat())
        s.setParam("master", "threshold", patch.limiterThreshold)
        s.setParam("master", "gain", patch.masterGain)
        s.setParam("master", "motion", patch.motionDepth)
        s.setParam("master", "motionRate", patch.motionRate)
        s.setParam("master", "saturation", patch.saturation)
        s.setParam("master", "intensity", patch.intensity)
        s.setParam("master", "pump", patch.pump)
        s.setKaoss(
            patch.kaoss.program, patch.kaoss.x, patch.kaoss.y,
            patch.kaoss.latch, patch.kaoss.mix,
        )
    }

    fun setStep(i: Int, on: Boolean, voice: Int, pitch: Float, velocity: Float, probability: Float) =
        service?.setStep(i, on, voice, pitch, velocity, probability)

    fun setTempo(bpm: Float, swing: Float) = service?.setTempo(bpm, swing)

    fun setKaoss(program: Int, x: Float, y: Float, latch: Boolean, mix: Float) =
        service?.setKaoss(program, x, y, latch, mix)

    fun triggerGlitch() = service?.triggerGlitch()
    fun triggerGranular() = service?.triggerGranular()

    // ---- Amostra do usuário (WAV local) ----
    var sampleName: String = ""
        private set
    var sampleMix: Float = 0.70f
    var sampleTune: Float = 0f
    var sampleStart: Float = 0f
    var sampleLoop: Boolean = true

    private var pendingSample: FloatArray? = null
    private var pendingSampleRate: Int = 48_000

    fun loadSample(samples: FloatArray, sampleRate: Int, displayName: String) {
        sampleName = displayName
        pendingSample = samples
        pendingSampleRate = sampleRate
        flushSample()
        applySampleParams()
    }

    private fun flushSample() {
        val s = service ?: return
        val data = pendingSample ?: return
        if (s.loadSample(data, pendingSampleRate)) {
            pendingSample = null
            s.setParam("sample", "trigger", 1f)
        }
    }

    fun applySampleParams() {
        val s = service ?: return
        s.setParam("sample", "mix", sampleMix)
        s.setParam("sample", "tune", sampleTune)
        s.setParam("sample", "start", sampleStart)
        s.setParam("sample", "loop", if (sampleLoop) 1f else 0f)
    }

    fun triggerSample() = service?.setParam("sample", "trigger", 1f)

    fun startRecording(path: String): Boolean = service?.startRecording(path) ?: false
    fun stopRecording(): String? = service?.stopRecording()
    fun setStream(host: String, port: Int): Boolean = service?.setStream(host, port) ?: false
    fun stopStream() = service?.stopStream()
    fun meter(): FloatArray = service?.meter() ?: floatArrayOf(0f, 0f)
}
