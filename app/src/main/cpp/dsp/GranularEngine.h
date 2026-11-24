#pragma once
#include <array>
#include <vector>
#include <cmath>
#include "Utils.h"

namespace hn {

/**
 * Síntese granular em tempo real — texturas de fita, "squeal" e pitch drop.
 *
 * modos:
 *   0 = scatter  — grãos espalhados no histórico (texture/rasgos)
 *   1 = freeze   — todos os grãos leem do mesmo ponto (portadora estática)
 *   2 = reverse  — leitura de trás para frente
 *   3 = pitchDrop— cada grão desacelera (tape stop / transformação)
 *
 * O histórico é um ring buffer pré-alocado de 3 s. Um escalonador dispara
 * `density` grãos por segundo; até 24 vozes convivem, cada uma com janela Hann
 * para evitar clicks. Não há alocação na thread de áudio.
 */
class GranularEngine {
public:
    explicit GranularEngine(float sr)
        : sr_(sr), buf_(static_cast<size_t>(sr * 3.f) + 32, 0.f) {}

    void setParams(int mode, float sizeMs, float density, float pitch,
                   float spread, float feedback, float mix) {
        int m = clamp(mode, 0, 3);
        if (m != mode_) freezeAnchor_ = -1.f; // reseta o ponto de freeze ao trocar de modo
        mode_ = m;
        sizeMs_ = clamp(sizeMs, 20.f, 800.f);
        density_ = clamp(density, 1.f, 80.f);
        pitch_ = clamp(pitch, -24.f, 24.f);
        spread_ = clamp(spread, 0.f, 1.f);
        feedback_ = clamp(feedback, 0.f, 0.85f);
        mix_ = clamp(mix, 0.f, 1.f);
        rate_ = std::pow(2.f, pitch_ / 12.f);
        interval_ = sr_ / density_;
    }

    /** Rajada manual (botão na UI): dispara alguns grãos de uma vez. */
    void trigger() {
        size_t anchor = (wp_ + buf_.size() - static_cast<size_t>(0.05f * sr_)) % buf_.size();
        for (int i = 0; i < 4; ++i) spawn_(anchor, true);
    }

    float process(float in) {
        const size_t w = wp_;
        buf_[w] = in;

        sched_ += 1.f;
        if (sched_ >= interval_) {
            sched_ -= interval_;
            spawn_(wp_, false);
        }

        float sum = 0.f;
        for (auto& g : grains_) {
            if (!g.active) continue;
            if (g.phase >= g.len) { g.active = false; continue; }

            const float frac = g.phase / std::max(1.f, g.len);
            const float window = 0.5f - 0.5f * std::cos(6.2831853f * frac); // Hann
            float rate = g.rate;
            if (mode_ == 3) rate *= lerp(1.f, 0.25f, frac); // tape stop dentro do grão

            g.pos += rate;
            float p = std::fmod(g.pos, (float)buf_.size());
            if (p < 0.f) p += (float)buf_.size();
            size_t idx = static_cast<size_t>(p);
            sum += buf_[idx] * window * g.amp;
            g.phase += 1.f;
        }

        const float wet = clamp(sum * 0.7f, -1.2f, 1.2f);
        if (feedback_ > 0.f && mix_ > 0.f) {
            buf_[w] = clamp(in + wet * feedback_, -1.f, 1.f);
        }
        wp_ = (w + 1) % buf_.size();
        return lerp(in, wet, mix_);
    }

private:
    struct Grain {
        bool active = false;
        float pos = 0.f;   // posição no ring buffer (float para permitir pitch fractional)
        float phase = 0.f; // amostras decorridas
        float len = 1.f;
        float rate = 1.f;  // positivo = forward, negativo = reverse
        float amp = 1.f;
    };

    void spawn_(size_t anchor, bool manual) {
        for (auto& g : grains_) {
            if (g.active) continue;

            const float r = (rng_.next() * 0.5f + 0.5f); // 0..1
            const float jitterS = r * spread_ * std::min(0.4f * sr_, (float)buf_.size() * 0.25f);
            size_t start = (anchor + buf_.size() - static_cast<size_t>(jitterS)) % buf_.size();

            float len = clamp(sizeMs_ * 0.001f * sr_, 32.f, (float)buf_.size() - 32.f);
            if (manual) len *= 0.6f;

            float rate = rate_;
            if (mode_ == 1) { // freeze: fixa todos os grãos no mesmo ponto
                if (freezeAnchor_ < 0.f) freezeAnchor_ = (float)start;
                start = static_cast<size_t>(freezeAnchor_);
            } else if (mode_ == 2) { // reverse: começa no fim da janela e volta
                rate = -rate;
                start = (start + static_cast<size_t>(len)) % buf_.size();
            }

            g.pos = (float)start;
            g.phase = 0.f;
            g.len = len;
            g.rate = rate;
            g.amp = 0.7f + 0.3f * (rng_.next() * 0.5f + 0.5f);
            g.active = true;
            return;
        }
    }

    float sr_;
    std::vector<float> buf_;
    size_t wp_ = 0;
    std::array<Grain, 24> grains_;

    float sched_ = 0.f, interval_ = 4000.f, rate_ = 1.f;
    float freezeAnchor_ = -1.f;
    int mode_ = 0;
    float sizeMs_ = 80.f, density_ = 12.f, pitch_ = 0.f;
    float spread_ = 0.4f, feedback_ = 0.2f, mix_ = 0.f;
    RNG rng_;
};

} // namespace hn
