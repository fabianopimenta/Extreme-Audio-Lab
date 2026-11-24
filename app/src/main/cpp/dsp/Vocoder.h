#pragma once
#include <array>
#include <cmath>
#include "Utils.h"

namespace hn {

/**
 * Vocoder de 12 bandas (formantes) — a "voz de robô" falando.
 *
 * O microfone é o modulador: cada banda extrai um envelope da voz/feedback.
 * A portadora pode ser o ruído do gerador, um oscilador interno ou ambos.
 * Envelopes rápidos dão o caráter robótico; a transposição `shift` permite
 * transformar a voz em "Prime" (suave) ou "Starscream" (aguda).
 *
 * `process()` devolve apenas o sinal vocodado; o blend com o mix original é
 * feito no AudioEngine através de `vocoderMix`.
 */
class Vocoder {
public:
    explicit Vocoder(float sr) : sr_(sr) {}

    void setParams(float pitch, float shift, float speed, float q, int source) {
        pitch_ = clamp(pitch, 40.f, 1200.f);
        shift_ = clamp(shift, -24.f, 24.f);
        speed_ = clamp(speed, 0.01f, 1.f);
        q_ = clamp(q, 0.5f, 12.f);
        source_ = std::max(0, std::min(2, source));

        scale_ = std::pow(2.f, shift_ / 12.f);
        phaseInc_ = 6.2831853f * pitch_ / sr_;

        const float attack = lerp(0.04f, 0.002f, speed_);
        const float release = lerp(0.25f, 0.02f, speed_);
        envA_ = std::exp(-1.f / (sr_ * attack));
        envR_ = std::exp(-1.f / (sr_ * release));
    }

    float process(float modulator, float noiseCarrier) {
        float osc = 0.f;
        if (source_ != 0) {
            phase_ += phaseInc_;
            if (phase_ >= 6.2831853f) phase_ -= 6.2831853f;
            osc = fastTanh(std::sin(phase_) * 2.f) * 0.45f; // harmonicamente rico
        }

        float carrier = noiseCarrier;
        if (source_ == 1) carrier = osc;
        else if (source_ == 2) carrier = clamp(noiseCarrier * 0.6f + osc, -1.f, 1.f);

        float sum = 0.f;
        for (size_t i = 0; i < formants_.size(); ++i) {
            const float fc = clamp(formants_[i] * scale_, 30.f, sr_ * 0.45f);

            modBands_[i].process(modulator, fc, q_, sr_);
            carBands_[i].process(carrier, fc, std::max(2.f, q_ * 0.6f), sr_);

            // Envelope do modulador com ataque/release assimétricos.
            const float mag = std::abs(modBands_[i].band) * (1.f + i * 0.12f);
            const float coef = mag > env_[i] ? envA_ : envR_;
            env_[i] = coef * env_[i] + (1.f - coef) * mag;

            sum += carBands_[i].band * env_[i] * gains_[i];
        }
        return clamp(sum * 3.5f, -1.f, 1.f);
    }

private:
    static constexpr size_t BANDS = 12;
    float sr_;
    std::array<SVF, BANDS> modBands_, carBands_;
    std::array<float, BANDS> env_{};

    float phase_ = 0.f, phaseInc_ = 0.f;
    float pitch_ = 110.f, shift_ = 0.f, speed_ = 0.5f, q_ = 3.f;
    float scale_ = 1.f, envA_ = 0.9f, envR_ = 0.98f;
    int source_ = 0;

    // Formantes aproximados de vogais + bandas altas para metallicidade.
    static constexpr float formants_[BANDS] = {
        180.f, 320.f, 520.f, 760.f, 1080.f, 1450.f,
        1900.f, 2450.f, 3100.f, 3900.f, 4900.f, 6200.f
    };
    static constexpr float gains_[BANDS] = {
        1.f, 0.95f, 0.9f, 0.85f, 0.75f, 0.65f,
        0.55f, 0.45f, 0.36f, 0.28f, 0.22f, 0.16f
    };
};

} // namespace hn
