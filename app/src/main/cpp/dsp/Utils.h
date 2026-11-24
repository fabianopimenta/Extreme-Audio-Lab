#pragma once
#include <cmath>
#include <cstdint>
#include <algorithm>

namespace hn {
inline float clamp(float v, float lo, float hi) { return std::max(lo, std::min(hi, v)); }
inline float lerp(float a, float b, float t) { return a + (b - a) * t; }
inline float dbToGain(float db) { return std::pow(10.f, db / 20.f); }

// tanh barato e estável o suficiente para distorção em tempo real.
inline float fastTanh(float x) {
    x = clamp(x, -4.f, 4.f);
    return x * (27.f + x * x) / (27.f + 9.f * x * x);
}

struct RNG {
    uint32_t state = 0x9e3779b9u;
    float next() {
        state ^= state << 13; state ^= state >> 17; state ^= state << 5;
        return (float)(state >> 8) / 16777216.f * 2.f - 1.f;
    }
};

struct DCBlock {
    float x1 = 0.f, y1 = 0.f;
    float process(float x) {
        float y = x - x1 + 0.995f * y1;
        x1 = x; y1 = y;
        return y;
    }
};

struct OnePole {
    float a = 0.9f, b = 0.1f, y = 0.f;
    void setCutoff(float fc, float sr) {
        float x = std::exp(-6.2831853f * clamp(fc, 10.f, sr * 0.45f) / sr);
        a = x; b = 1.f - x;
    }
    float process(float in) { y = a * y + b * in; return y; }
};

struct SVF {
    float low = 0.f, band = 0.f, high = 0.f;
    void process(float in, float cutoff, float resonance, float sr) {
        float f = 2.f * std::sin(3.14159265f * clamp(cutoff, 20.f, sr * 0.45f) / sr);
        float q = 1.f / clamp(resonance, 0.5f, 12.f);
        low += f * band;
        high = in - low - q * band;
        band += f * high;
    }
    /** Injeta energia diretamente no estado do filtro (excitação de ressonadores). */
    void excite(float amount) { band += amount; low += amount * 0.25f; }
};
}
