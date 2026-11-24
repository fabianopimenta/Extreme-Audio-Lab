#pragma once
#include "Utils.h"

namespace hn {

/**
 * Limitador de pico transparente: só intervém quando o sinal passa do teto.
 * Abaixo do teto a passagem é linear — importante para que os faders alterem
 * o volume de verdade em vez de serem "achatados" por compressão constante.
 */
class Limiter {
public:
    float process(float in, float thresholdDb, float& reduction) {
        float th = dbToGain(clamp(thresholdDb, -12.f, 0.f));
        env_ = std::max(std::fabs(in), env_ * 0.9975f); // release
        float target = env_ > th ? th / env_ : 1.f;
        gain_ = lerp(gain_, target, target < gain_ ? 0.30f : 0.035f);
        reduction = 1.f - gain_;
        return clamp(in * gain_, -1.f, 1.f);
    }

private:
    float env_ = 0.f, gain_ = 1.f;
};
}
