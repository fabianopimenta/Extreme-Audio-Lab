package com.extremeaudiolab.audio

/** Ponte JNI para o motor C++/Oboe. Nenhuma chamada de rede externa. */
class NativeEngine {
    companion object {
        init { System.loadLibrary("extreme_audio") }
    }

    external fun nativeCreate(sampleRate: Int, framesPerBuffer: Int): Long
    external fun nativeDestroy(handle: Long)
    external fun nativeStart(handle: Long): Boolean
    external fun nativeStop(handle: Long)

    external fun nativeSetParam(handle: Long, module: String, param: String, value: Float)
    external fun nativeSetStep(
        handle: Long, index: Int, on: Boolean, voice: Int,
        pitch: Float, velocity: Float, probability: Float,
    )
    external fun nativeSetTempo(handle: Long, bpm: Float, swing: Float)
    external fun nativeSetKaoss(
        handle: Long, program: Int, x: Float, y: Float, latch: Boolean, mix: Float,
    )
    external fun nativeTriggerGlitch(handle: Long)
    external fun nativeTriggerGranular(handle: Long)

    external fun nativeStartRecording(handle: Long, path: String): Boolean
    external fun nativeStopRecording(handle: Long): String?
    external fun nativeSetStream(handle: Long, host: String, port: Int): Boolean
    external fun nativeStopStream(handle: Long)
    external fun nativeMeter(handle: Long): FloatArray
}
