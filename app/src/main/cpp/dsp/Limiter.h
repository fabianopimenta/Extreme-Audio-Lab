#pragma once
#include "Utils.h"

namespace hn {

/** Limitador de pico com soft-clip final e medidor de redução de ganho. */
class Limiter {
public:
    float process(float in, float thresholdDb, float& reduction) {
        float th = dbToGain(clamp(thresholdDb, -24.f, 0.f));
        env_ = std::max(std::fabs(in), env_ * 0.994f); // release
        float gain = env_ > th ? th / env_ : 1.f;
        gain_ = lerp(gain_, gain, env_ > gain_ * th ? 0.35f : 0.04f);
        float out = fastTanh(in * gain_ * 1.05f);
        reduction = 1.f - gain_;
        return clamp(out, -1.f, 1.f);
    }

private:
    float env_ = 0.f, gain_ = 1.f;
};
}
