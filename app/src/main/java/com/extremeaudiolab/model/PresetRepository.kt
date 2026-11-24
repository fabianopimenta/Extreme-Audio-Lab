package com.extremeaudiolab.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Preset(val name: String, val description: String, val patch: Patch)

/** Lê os presets de `assets/patches/default.json` usando apenas APIs do Android. */
object PresetRepository {
    private const val ASSET = "patches/default.json"

    fun load(context: Context): List<Preset> {
        val json = context.assets.open(ASSET).bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val result = mutableListOf(
            Preset(root.optString("name", "Default"), root.optString("description", ""), parse(root))
        )
        val array = root.optJSONArray("presets") ?: JSONArray()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            result += Preset(
                obj.optString("name", "Preset ${i + 1}"),
                obj.optString("description", ""),
                parse(obj),
            )
        }
        return result
    }

    private fun parse(o: JSONObject): Patch {
        val steps = mutableListOf<Step>()
        val arr = o.optJSONArray("steps") ?: JSONArray()
        for (i in 0 until 16) {
            val s = arr.optJSONObject(i)
            steps += if (s == null) Step() else Step(
                on = s.optBoolean("on", false),
                voice = s.optInt("voice", 0).coerceIn(0, 5),
                pitch = s.optDouble("pitch", 0.0).toFloat(),
                velocity = s.optDouble("velocity", 1.0).toFloat(),
                probability = s.optDouble("probability", 1.0).toFloat(),
            )
        }
        val k = o.optJSONObject("kaoss") ?: JSONObject()
        return Patch(
            noiseType = o.optInt("noiseType", 0),
            noiseLevel = o.optDouble("noiseLevel", 0.35).toFloat(),
            noiseCrush = o.optDouble("noiseCrush", 16.0).toFloat(),
            noiseHold = o.optDouble("noiseHold", 40.0).toFloat(),
            noiseComb = o.optDouble("noiseComb", 0.25).toFloat(),
            micGain = o.optDouble("micGain", 0.72).toFloat(),
            micDelayMs = o.optDouble("micDelayMs", 18.0).toFloat(),
            micTone = o.optDouble("micTone", 1200.0).toFloat(),
            micMix = o.optDouble("micMix", 0.5).toFloat(),
            micGate = o.optDouble("micGate", 0.08).toFloat(),
            bpm = o.optDouble("bpm", 138.0).toFloat(),
            swing = o.optDouble("swing", 0.12).toFloat(),
            steps = steps,
            fuzzDrive = o.optDouble("fuzzDrive", 0.8).toFloat(),
            fuzzOctave = o.optDouble("fuzzOctave", 0.35).toFloat(),
            fuzzGate = o.optDouble("fuzzGate", 0.2).toFloat(),
            fuzzTone = o.optDouble("fuzzTone", 0.55).toFloat(),
            dodGain = o.optDouble("dodGain", 0.85).toFloat(),
            dodLowCut = o.optDouble("dodLowCut", 0.25).toFloat(),
            dodScoop = o.optDouble("dodScoop", 0.45).toFloat(),
            dodPresence = o.optDouble("dodPresence", 0.6).toFloat(),
            dodGate = o.optDouble("dodGate", 0.15).toFloat(),
            glitchMode = o.optInt("glitchMode", 0),
            glitchRate = o.optDouble("glitchRate", 6.0).toFloat(),
            glitchSize = o.optDouble("glitchSize", 60.0).toFloat(),
            glitchDecimate = o.optDouble("glitchDecimate", 8.0).toFloat(),
            glitchBits = o.optDouble("glitchBits", 8.0).toFloat(),
            glitchMix = o.optDouble("glitchMix", 0.45).toFloat(),
            glitchProbability = o.optDouble("glitchProbability", 0.35).toFloat(),
            glitchGate = o.optDouble("glitchGate", 0.3).toFloat(),
            modalTune = o.optDouble("modalTune", 220.0).toFloat(),
            modalInharmonicity = o.optDouble("modalInharmonicity", 0.6).toFloat(),
            modalDecay = o.optDouble("modalDecay", 0.7).toFloat(),
            modalBrightness = o.optDouble("modalBrightness", 0.5).toFloat(),
            modalMix = o.optDouble("modalMix", 0.3).toFloat(),
            granularMode = o.optInt("granularMode", 0),
            granularSize = o.optDouble("granularSize", 80.0).toFloat(),
            granularDensity = o.optDouble("granularDensity", 12.0).toFloat(),
            granularPitch = o.optDouble("granularPitch", 0.0).toFloat(),
            granularSpread = o.optDouble("granularSpread", 0.4).toFloat(),
            granularFeedback = o.optDouble("granularFeedback", 0.2).toFloat(),
            granularMix = o.optDouble("granularMix", 0.0).toFloat(),
            vocoderSource = o.optInt("vocoderSource", 0),
            vocoderPitch = o.optDouble("vocoderPitch", 110.0).toFloat(),
            vocoderShift = o.optDouble("vocoderShift", 0.0).toFloat(),
            vocoderSpeed = o.optDouble("vocoderSpeed", 0.5).toFloat(),
            vocoderQ = o.optDouble("vocoderQ", 3.0).toFloat(),
            vocoderMix = o.optDouble("vocoderMix", 0.0).toFloat(),
            destructionMode = o.optInt("destructionMode", 0),
            destructionDrive = o.optDouble("destructionDrive", 4.0).toFloat(),
            destructionWet = o.optDouble("destructionWet", 0.0).toFloat(),
            modEnabled = o.optBoolean("modEnabled", false),
            modRate = o.optDouble("modRate", 2.0).toFloat(),
            modDepth = o.optDouble("modDepth", 0.0).toFloat(),
            modDestination = o.optInt("modDestination", 0),
            modShape = o.optInt("modShape", 0),
            subFrequency = o.optDouble("subFrequency", 55.0).toFloat(),
            subLevel = o.optDouble("subLevel", 0.0).toFloat(),
            subWave = o.optInt("subWave", 0),
            synthFrequency = o.optDouble("synthFrequency", 110.0).toFloat(),
            synthLevel = o.optDouble("synthLevel", 0.0).toFloat(),
            synthWave = o.optInt("synthWave", 3),
            kaoss = KaossState(
                program = k.optInt("program", 0),
                x = k.optDouble("x", 0.5).toFloat(),
                y = k.optDouble("y", 0.5).toFloat(),
                latch = k.optBoolean("latch", false),
                mix = k.optDouble("mix", 1.0).toFloat(),
            ),
            limiterThreshold = o.optDouble("limiterThreshold", -3.0).toFloat(),
            masterGain = o.optDouble("masterGain", 0.7).toFloat(),
        )
    }
}
