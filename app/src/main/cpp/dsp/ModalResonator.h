#pragma once
#include <array>
#include "Utils.h"

namespace hn {

/**
 * Banco de 6 ressonadores modais — clanks, sinos e "serviços" metálicos.
 * Inspirado em síntese modal (Mutable Rings): cada modo é um SVF ressonante
 * com frequência inarmônica e amortecimento próprio.
 *
 * Transientes na entrada injetam energia nos modos (excitação automática),
 * e `excite()` permite disparo manual pelo botão da UI.
 */
class ModalResonator {
public:
    explicit ModalResonator(float sr) : sr_(sr) {}

    void setParams(float tune, float inharmonicity, float decay, float brightness, float mix) {
        tune_ = clamp(tune, 40.f, 2000.f);
        inharm_ = clamp(inharmonicity, 0.f, 1.f);
        decay_ = clamp(decay, 0.f, 1.f);
        brightness_ = clamp(brightness, 0.f, 1.f);
        mix_ = clamp(mix, 0.f, 1.f);
    }

    void excite(float amount = 1.f) {
        for (auto& m : modes_) m.svf.excite(amount);
    }

    float process(float in) {
        // Detecção de transiente: ataque rápido dispara os modos.
        float delta = in - prev_;
        prev_ = in;
        if (delta > 0.10f) excite(std::min(2.f, delta * 1.5f));

        float q = 3.f + decay_ * 32.f;
        float sum = 0.f;
        for (size_t i = 0; i < modes_.size(); ++i) {
            float ratio = base_[i] + inharm_ * shift_[i];
            float fc = clamp(tune_ * ratio, 40.f, sr_ * 0.45f);
            // Modos agudos amortecem mais rápido (damping dependente da frequência).
            float damp = clamp(fc * (0.35f + brightness_ * 2.2f), 90.f, sr_ * 0.45f);
            modes_[i].lp.setCutoff(damp, sr_);
            modes_[i].svf.process(in, fc, q, sr_);
            sum += modes_[i].lp.process(modes_[i].svf.band) * gain_[i];
        }
        return lerp(in, clamp(sum * 1.6f, -1.2f, 1.2f), mix_);
    }

private:
    struct Mode { SVF svf; OnePole lp; };

    float sr_;
    std::array<Mode, 6> modes_;
    float prev_ = 0.f;
    float tune_ = 220.f, inharm_ = 0.6f, decay_ = 0.7f, brightness_ = 0.5f, mix_ = 0.3f;

    // Razões inarmônicas de sino/metal; shift_ controla o desvio com inharmonicity.
    static constexpr float base_[6]   = {1.f, 1.59f, 2.14f, 2.83f, 3.76f, 4.77f};
    static constexpr float shift_[6]  = {0.f, 0.12f, 0.31f, 0.48f, 0.72f, 1.05f};
    static constexpr float gain_[6]   = {1.f, 0.72f, 0.55f, 0.40f, 0.28f, 0.18f};
};
}
