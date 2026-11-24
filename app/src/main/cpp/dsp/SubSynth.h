#pragma once
#include <cmath>
#include "Utils.h"

namespace hn {

/** Duas vozes tonais (Sub Bass + Synth) com 4 formas de onda e pitch estável. */
class SubSynth {
public:
    void setSub(float frequency, float level, int wave) {
        subFreq_ = clamp(frequency, 10.f, 200.f);
        subLevel_ = clamp(level, 0.f, 0.4f);
        subWave_ = (int)clamp((float)wave, 0.f, 3.f);
    }

    void setSynth(float frequency, float level, int wave) {
        synthFreq_ = clamp(frequency, 30.f, 1200.f);
        synthLevel_ = clamp(level, 0.f, 0.3f);
        synthWave_ = (int)clamp((float)wave, 0.f, 3.f);
    }

    void reset() { subPhase_ = 0.f; synthPhase_ = 0.f; }

    float process(float sampleRate, float subScale, float synthScale) {
        subPhase_ += subFreq_ / std::max(1.f, sampleRate);
        synthPhase_ += synthFreq_ / std::max(1.f, sampleRate);
        if (subPhase_ >= 1.f) subPhase_ -= std::floor(subPhase_);
        if (synthPhase_ >= 1.f) synthPhase_ -= std::floor(synthPhase_);
        const float sub = osc(subPhase_, subWave_) * subLevel_ * subScale;
        const float synth = osc(synthPhase_, synthWave_) * synthLevel_ * synthScale;
        return sub + synth;
    }

private:
    static float osc(float phase, int wave) {
        const float a = phase * 6.2831853f;
        switch (wave) {
            case 1: return static_cast<float>((2.f / 3.14159265f) * std::asin(std::sin(a)));
            case 2: return fastTanh(std::sin(a) * 8.f);
            case 3: return 2.f * phase - 1.f;
            default: return std::sin(a);
        }
    }

    float subFreq_ = 55.f, subLevel_ = 0.f;
    float synthFreq_ = 110.f, synthLevel_ = 0.f;
    int subWave_ = 0, synthWave_ = 3;
    float subPhase_ = 0.f, synthPhase_ = 0.f;
};

} // namespace hn
