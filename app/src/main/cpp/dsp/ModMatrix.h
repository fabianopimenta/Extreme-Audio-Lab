#pragma once
#include <cmath>
#include "Utils.h"

namespace hn {

/**
 * Mod Matrix (primeira etapa): um LFO com 4 formas e 4 destinos.
 * Destinos aplicados no AudioEngine: 0 ruído, 1 sub, 2 synth, 3 microfone.
 * 0 sine, 1 triangle, 2 square, 3 sample & hold.
 */
class ModMatrix {
public:
    void setParams(bool enabled, float rate, float depth, int destination, int shape) {
        enabled_ = enabled;
        rate_ = clamp(rate, 0.05f, 20.f);
        depth_ = clamp(depth, 0.f, 1.f);
        destination_ = (int)clamp((float)destination, 0.f, 3.f);
        shape_ = (int)clamp((float)shape, 0.f, 3.f);
    }

    void reset() {
        phase_ = 0.f;
        held_ = 0.f;
    }

    /** Devolve o valor normalizado do LFO (-1..1). */
    float next(float sampleRate) {
        if (!enabled_) return 0.f;
        float value = 0.f;
        switch (shape_) {
            case 1: value = 1.f - 4.f * std::fabs(phase_ - 0.5f); break;
            case 2: value = phase_ < 0.5f ? 1.f : -1.f; break;
            case 3: value = held_; break;
            default: value = std::sin(phase_ * 6.2831853f); break;
        }
        phase_ += rate_ / std::max(1.f, sampleRate);
        if (phase_ >= 1.f) {
            phase_ -= 1.f;
            if (shape_ == 3) held_ = rng_.next();
        }
        return value * depth_;
    }

    int destination() const { return destination_; }

private:
    bool enabled_ = false;
    float rate_ = 2.f;
    float depth_ = 0.f;
    int destination_ = 0;
    int shape_ = 0;
    float phase_ = 0.f;
    float held_ = 0.f;
    RNG rng_;
};

} // namespace hn
