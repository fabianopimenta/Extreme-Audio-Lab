#pragma once
#include <vector>
#include <cmath>
#include "Utils.h"

namespace hn {

/**
 * Processador de glitch digital com quatro modos e gate de breakdown embutido.
 *
 * modos:
 *   0 = stutter  — repete um slice curto do passado (engasgo rítmico)
 *   1 = reverse  — lê o histórico de trás para frente
 *   2 = freeze   — congela uma posição (portadora tonal / pitch freeze)
 *   3 = decimate — sample&hold + redução de bits ("dados corrompidos")
 *
 * Um clock interno dispara eventos com probabilidade configurável. Cada evento
 * tem fade de entrada/saída para evitar clicks, e expõe `activity()` para o
 * AudioEngine duckar a cadeia depois do Kaoss (o "breakdown").
 */
class GlitchProcessor {
public:
    explicit GlitchProcessor(float sr)
        : sr_(sr), buf_((size_t)(sr * 2.f) + 16, 0.f) {}

    void setParams(int mode, float rate, float sizeMs, float decimate, float bits,
                   float mix, float probability, float gateDepth) {
        mode_ = clamp(mode, 0, 3);
        rate_ = clamp(rate, 0.5f, 40.f);
        sizeMs_ = clamp(sizeMs, 10.f, 400.f);
        decimate_ = clamp(decimate, 1.f, 64.f);
        bits_ = clamp(bits, 2.f, 16.f);
        mix_ = clamp(mix, 0.f, 1.f);
        probability_ = clamp(probability, 0.f, 1.f);
        gateDepth_ = clamp(gateDepth, 0.f, 1.f);
        period_ = sr_ / rate_;
    }

    /** Dispara um evento manualmente (botão "Glitch" na UI). */
    void trigger() { startEvent_(); }

    /** 0..1 — quanto o breakdown está duckando a cadeia neste instante. */
    float activity() const { return gate_; }
    bool active() const { return remaining_ > 0; }

    float process(float in) {
        buf_[wp_] = in;
        wp_ = (wp_ + 1) % buf_.size();

        phase_ += 1.f;
        if (phase_ >= period_) {
            phase_ -= period_;
            if ((rng_.next() + 1.f) * 0.5f <= probability_) startEvent_();
        }

        float out = in;
        if (remaining_ > 0) {
            float played = (float)(eventLen_ - remaining_);
            float wet = read_(in, played);
            float fade = clamp(std::min(played, (float)remaining_) / 48.f, 0.f, 1.f);
            out = lerp(in, wet, fade * mix_);
            --remaining_;
        }

        float target = remaining_ > 0 ? gateDepth_ : 0.f;
        gate_ += (target - gate_) * (target > gate_ ? 0.30f : 0.06f);
        return out;
    }

private:
    void startEvent_() {
        sliceLen_ = (size_t)clamp(sizeMs_ * 0.001f * sr_, 16.f, (float)buf_.size() - 16.f);
        eventLen_ = sliceLen_ * (mode_ == 0 ? 3u : (mode_ == 2 ? 2u : 1u));
        loopStart_ = (wp_ + buf_.size() - sliceLen_ - 1) % buf_.size();
        remaining_ = eventLen_;
        holdCount_ = 0;
    }

    float read_(float in, float played) {
        switch (mode_) {
            case 0: { // stutter: repete o slice
                float slice = std::max(1.f, (float)sliceLen_);
                size_t off = (size_t)std::fmod(played, slice);
                return buf_[(loopStart_ + off) % buf_.size()];
            }
            case 1: { // reverse: histórico recente de trás pra frente
                size_t off = (size_t)std::min(played, (float)(buf_.size() - 4));
                return buf_[(wp_ + buf_.size() - 1 - off) % buf_.size()];
            }
            case 2: // freeze: portadora estacionária
                return buf_[loopStart_];
            default: { // decimate: sample&hold + bit reduction
                if (++holdCount_ >= (int)decimate_) {
                    holdCount_ = 0;
                    held_ = in;
                }
                float steps = std::pow(2.f, bits_ - 1.f);
                return std::round(held_ * steps) / steps;
            }
        }
    }

    float sr_;
    std::vector<float> buf_;
    size_t wp_ = 0, loopStart_ = 0, sliceLen_ = 256, eventLen_ = 256;
    size_t remaining_ = 0;
    float phase_ = 0.f, period_ = 8000.f;
    float mix_ = 0.45f, probability_ = 0.35f, gateDepth_ = 0.3f;
    float sizeMs_ = 60.f, decimate_ = 8.f, bits_ = 8.f;
    int mode_ = 0;
    int holdCount_ = 0;
    float held_ = 0.f, gate_ = 0.f;
    RNG rng_;
};
}
