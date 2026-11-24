#pragma once
#include <cmath>
#include "Utils.h"

namespace hn {

struct StepData {
    bool on = false;
    int voice = 0; // 0 kick, 1 snare, 2 hat, 3 tom, 4 industrial, 5 noise burst
    float pitch = 0.f;
    float velocity = 1.f;
    float probability = 1.f;
};

/** Sequenciador de 16 passos com pitch/velocity/probabilidade por passo. */
class DrumSequencer {
public:
    explicit DrumSequencer(float sr) : sr_(sr) {}

    void setTempo(float bpm, float swing) {
        stepFrames_ = sr_ * 60.f / std::max(20.f, bpm) / 4.f;
        swing_ = clamp(swing, 0.f, 0.6f);
    }

    /** Chamado no início de cada bloco para trocar de passo. */
    void tick(const StepData& step) {
        if (phase_ >= stepFrames_) {
            phase_ -= stepFrames_;
            current_ = (current_ + 1) & 15;
            if (step.on && (rng_.next() + 1.f) * 0.5f <= step.probability) {
                trigger(step.voice, step.pitch, step.velocity);
            }
        }
    }

    float process(float noise) {
        float out = 0.f;
        if (env_ > 0.0001f) {
            float ratio = std::pow(2.f, pitch_ / 12.f);
            switch (voice_) {
                case 0: { // kick
                    freq_ = lerp(48.f * ratio, 140.f * ratio, env_ * env_);
                    phase_osc_ += 6.2831853f * freq_ / sr_;
                    out = std::sin(phase_osc_) * env_;
                    break;
                }
                case 1: { // snare
                    phase_osc_ += 6.2831853f * 180.f * ratio / sr_;
                    out = (noise * 0.8f + std::sin(phase_osc_) * 0.4f) * env_;
                    break;
                }
                case 2: { // hat
                    hp_.setCutoff(6000.f * ratio, sr_);
                    out = (noise - hp_.process(noise)) * env_ * 1.4f;
                    break;
                }
                case 3: { // tom
                    freq_ = lerp(90.f * ratio, 220.f * ratio, env_);
                    phase_osc_ += 6.2831853f * freq_ / sr_;
                    out = std::sin(phase_osc_) * env_;
                    break;
                }
                case 4: { // industrial hit — parciais inarmônicos
                    phase_osc_ += 6.2831853f * 173.f * ratio / sr_;
                    const float p = phase_osc_;
                    out = (std::sin(p) * 0.55f + std::sin(p * 2.71f) * 0.30f +
                           std::sin(p * 4.13f) * 0.15f) * env_;
                    break;
                }
                default: { // noise burst
                    out = noise * env_ * 0.9f;
                }
            }
            env_ *= decay_;
        }
        phase_ += 1.f + (current_ & 1 ? swing_ * 0.5f : -swing_ * 0.5f);
        return out;
    }

    int currentStep() const { return current_; }

private:
    void trigger(int voice, float pitch, float velocity) {
        voice_ = voice; pitch_ = pitch; env_ = clamp(velocity, 0.f, 1.f);
        phase_osc_ = 0.f;
        decay_ = voice == 2 ? 0.986f : (voice == 5 ? 0.975f : (voice == 4 ? 0.998f : 0.995f));
    }

    float sr_;
    float stepFrames_ = 5000.f;
    float phase_ = 0.f;
    int current_ = -1;
    float swing_ = 0.f;
    RNG rng_;
    int voice_ = 0;
    float pitch_ = 0.f, env_ = 0.f, decay_ = 0.995f;
    float phase_osc_ = 0.f, freq_ = 100.f;
    OnePole hp_;
};
}
