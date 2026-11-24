#pragma once
#include "Utils.h"

namespace hn {

/** Fuzz agressivo com octave-up, clipping assimétrico e gate. */
class HyperFuzz {
public:
    void setParams(float drive, float octave, float gate, float tone) {
        drive_ = drive; octave_ = octave; gate_ = gate; tone_ = tone;
    }

    float process(float in) {
        float d = 1.f + drive_ * 14.f;
        float pre = fastTanh(in * d);

        // Octave-up por retificação + filtro.
        float rect = std::fabs(pre) * 2.f - 1.f;
        octLp_.setCutoff(900.f + octave_ * 4200.f, 48000.f);
        float oct = octLp_.process(rect);
        float body = lerp(pre, pre * 0.55f + oct * 0.9f, octave_);

        // Clipping assimétrico (fuzz "desigual" típico de circuitos germanados).
        float clipped = body > 0.f
            ? fastTanh(body * (1.f + drive_ * 5.f))
            : fastTanh(body * (1.f + drive_ * 9.f)) * 0.78f;

        // Gate segue a envoltória da entrada.
        env_ = std::max(std::fabs(in), env_ * 0.992f);
        float th = gate_ * 0.35f;
        float g = th <= 0.f ? 1.f : clamp(env_ / th, 0.f, 1.f);

        toneLp_.setCutoff(1200.f + tone_ * 11000.f, 48000.f);
        float out = toneLp_.process(clipped) * g;
        return clamp(out * 1.2f, -1.f, 1.f);
    }

private:
    float drive_ = 0.8f, octave_ = 0.35f, gate_ = 0.2f, tone_ = 0.55f;
    float env_ = 0.f;
    OnePole octLp_, toneLp_;
};
}
