#pragma once
#include "Utils.h"

namespace hn {

/** Emulação de pedal death-metal: alto ganho, low cut, mid scoop e gate. */
class DODDeathMetal {
public:
    void setParams(float gain, float lowCut, float scoop, float presence, float gate) {
        gain_ = gain; lowCut_ = lowCut; scoop_ = scoop; presence_ = presence; gate_ = gate;
    }

    float process(float in) {
        hp_.setCutoff(35.f + lowCut_ * 260.f, 48000.f);
        float x = hp_.process(in);

        float driven = fastTanh(x * (1.f + gain_ * 22.f));
        // clip assimétrico adicional, bem "seco"
        driven = clamp(driven * 1.35f, -0.95f, 0.95f);

        lp_.setCutoff(420.f + (1.f - scoop_) * 900.f, 48000.f);
        hp2_.setCutoff(1800.f + presence_ * 2600.f, 48000.f);
        float lows = lp_.process(driven);
        float highs = hp2_.process(driven) - driven; // high shelf aproximado
        float scooped = (lows + driven + highs * 0.5f) * 0.5f;
        float out = lerp(driven, scooped, scoop_);

        env_ = std::max(std::fabs(in), env_ * 0.989f);
        float th = gate_ * 0.3f;
        out *= th <= 0.f ? 1.f : clamp(env_ / th, 0.f, 1.f);
        return clamp(out * 0.85f, -1.f, 1.f);
    }

private:
    float gain_ = 0.85f, lowCut_ = 0.25f, scoop_ = 0.45f, presence_ = 0.6f, gate_ = 0.15f;
    float env_ = 0.f;
    OnePole hp_, lp_, hp2_;
};
}
