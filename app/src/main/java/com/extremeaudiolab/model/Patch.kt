package com.extremeaudiolab.model

data class Step(
    val on: Boolean = false,
    val voice: Int = 0, // 0 kick, 1 snare, 2 hat, 3 tom, 4 industrial, 5 noise burst
    val pitch: Float = 0f, // semitons
    val velocity: Float = 1f,
    val probability: Float = 1f,
)

data class KaossState(
    val program: Int = 0,
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val latch: Boolean = false,
    val mix: Float = 1f,
)

data class Patch(
    val noiseType: Int = 0,
    val noiseLevel: Float = 0.35f,
    val noiseCrush: Float = 16f,
    val noiseHold: Float = 40f,
    val noiseComb: Float = 0.25f,
    val micGain: Float = 0.72f,
    val micDelayMs: Float = 18f,
    val micTone: Float = 1200f,
    val micMix: Float = 0.5f,
    val micGate: Float = 0.08f,
    val bpm: Float = 138f,
    val swing: Float = 0.12f,
    val steps: List<Step> = List(16) { Step() },
    val fuzzDrive: Float = 0.8f,
    val fuzzOctave: Float = 0.35f,
    val fuzzGate: Float = 0.2f,
    val fuzzTone: Float = 0.55f,
    val dodGain: Float = 0.85f,
    val dodLowCut: Float = 0.25f,
    val dodScoop: Float = 0.45f,
    val dodPresence: Float = 0.6f,
    val dodGate: Float = 0.15f,
    val glitchMode: Int = 0, // 0 stutter, 1 reverse, 2 freeze, 3 decimate
    val glitchRate: Float = 6f,
    val glitchSize: Float = 60f, // ms
    val glitchDecimate: Float = 8f,
    val glitchBits: Float = 8f,
    val glitchMix: Float = 0.45f,
    val glitchProbability: Float = 0.35f,
    val glitchGate: Float = 0.3f,
    val modalTune: Float = 220f,
    val modalInharmonicity: Float = 0.6f,
    val modalDecay: Float = 0.7f,
    val modalBrightness: Float = 0.5f,
    val modalMix: Float = 0.3f,
    // Granular: 0 scatter, 1 freeze, 2 reverse, 3 pitchDrop
    val granularMode: Int = 0,
    val granularSize: Float = 80f, // ms
    val granularDensity: Float = 12f, // grãos/s
    val granularPitch: Float = 0f, // semitons
    val granularSpread: Float = 0.4f,
    val granularFeedback: Float = 0.2f,
    val granularMix: Float = 0f,
    // Vocoder: 0 ruído, 1 oscilador, 2 ambos
    val vocoderSource: Int = 0,
    val vocoderPitch: Float = 110f, // Hz do oscilador
    val vocoderShift: Float = 0f, // semitons dos formantes
    val vocoderSpeed: Float = 0.5f,
    val vocoderQ: Float = 3f,
    val vocoderMix: Float = 0f,
    // Extreme Audio Lab — Destruction Lab
    val destructionMode: Int = 0, // 0 bypass, 1 hard clip, 2 asymmetric, 3 wavefold, 4 bitcrush, 5 gate
    val destructionDrive: Float = 4f,
    val destructionWet: Float = 0f,
    // Extreme Audio Lab — Mod Matrix (LFO)
    val modEnabled: Boolean = false,
    val modRate: Float = 2f,
    val modDepth: Float = 0f,
    val modDestination: Int = 0, // 0 ruído, 1 sub, 2 synth, 3 microfone
    val modShape: Int = 0, // 0 sine, 1 tri, 2 square, 3 S&H
    // Extreme Audio Lab — Sub Bass + Synth
    val subFrequency: Float = 55f,
    val subLevel: Float = 0f,
    val subWave: Int = 0,
    val synthFrequency: Float = 110f,
    val synthLevel: Float = 0f,
    val synthWave: Int = 3,
    val kaoss: KaossState = KaossState(),
    val limiterThreshold: Float = -2f,
    val masterGain: Float = 0.7f,
    // Dinâmica: movimento lento (respiração), saturação, intensidade e pump.
    val motionDepth: Float = 0.55f,
    val motionRate: Float = 0.60f,
    val saturation: Float = 0.45f,
    val intensity: Float = 0.80f,
    val pump: Float = 0.40f,
)
