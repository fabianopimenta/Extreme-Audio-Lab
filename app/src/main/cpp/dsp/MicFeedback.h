#pragma once
#include <vector>
#include "Utils.h"

namespace hn {

/** Loop de realimentação de microfone com delay, filtro e gate. */
class MicFeedback {
public:
    explicit MicFeedback(float sr) : sr_(sr), delay_(sr * 0.25f > 64 ? (size_t)(sr * 0.25f) : 64, 0.f) {}

    void setParams(float gain, float delayMs, float tone, float mix, float gate) {
        gain_ = clamp(gain, 0.f, 0.95f);
        delaySamples_ = std::max(1.f, delayMs * 0.001f * sr_);
        tone_ = tone;
        mix_ = mix;
        gate_ = gate;
    }

    float process(float mic) {
        // Gate simples: corta o sinal abaixo do limiar.
        float gated = std::fabs(mic) > gate_ ? mic : mic * 0.05f;

        float rp = (float)wPos_ - delaySamples_;
        while (rp < 0) rp += (float)delay_.size();
        size_t i0 = (size_t)rp % delay_.size();
        size_t i1 = (i0 + 1) % delay_.size();
        float frac = rp - (float)(size_t)rp;
        float delayed = lerp(delay_[i0], delay_[i1], frac);

        svf_.process(delayed, tone_, 1.2f, sr_);
        float wet = svf_.band * gain_;

        float out = gated * mix_ + wet;
        float written = clamp(out, -1.5f, 1.5f);
        delay_[wPos_] = written;
        wPos_ = (wPos_ + 1) % delay_.size();
        return out;
    }

private:
    float sr_;
    std::vector<float> delay_;
    size_t wPos_ = 0;
    float delaySamples_ = 100.f;
    float gain_ = 0.72f, tone_ = 1200.f, mix_ = 0.5f, gate_ = 0.08f;
    SVF svf_;
};
}
