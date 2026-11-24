#pragma once
#include <vector>
#include <cmath>
#include "Utils.h"

namespace hn {

class ModDelay {
public:
    explicit ModDelay(float sr = 48000.f) : buf_((size_t)(sr * 2.f) + 8, 0.f), sr_(sr) {}
    float process(float in, float delayMs, float feedback, float wet) {
        float maxMs = (float)buf_.size() / sr_ * 1000.f;
        float ds = clamp(delayMs, 0.1f, maxMs - 2.f) * 0.001f * sr_;
        size_t i0 = (size_t)ds;
        float frac = ds - (float)i0;
        size_t r0 = (wp_ + buf_.size() - i0) % buf_.size();
        size_t r1 = (r0 + buf_.size() - 1) % buf_.size();
        float d = lerp(buf_[r0], buf_[r1], frac);
        float out = in + d * wet;
        buf_[wp_] = in + d * feedback;
        wp_ = (wp_ + 1) % buf_.size();
        return out;
    }
private:
    std::vector<float> buf_;
    size_t wp_ = 0;
    float sr_;
};

class AllPass {
public:
    float process(float in, float a) {
        float y = a * in + x1_ - a * y1_;
        x1_ = in; y1_ = y;
        return y;
    }
private:
    float x1_ = 0.f, y1_ = 0.f;
};

/** Schroeder reverb: 4 combs + 2 allpass. */
class Reverb {
public:
    explicit Reverb(float sr) {
        const float lens[4] = {0.0297f, 0.0371f, 0.0411f, 0.0437f};
        for (int i = 0; i < 4; ++i) combs_[i] = ModDelay(sr);
        (void)lens;
        sr_ = sr;
    }
    float process(float in, float size, float mix) {
        float sum = 0.f;
        const float base[4] = {29.7f, 37.1f, 41.1f, 43.7f};
        for (int i = 0; i < 4; ++i) {
            float t = base[i] * (0.5f + size * 2.5f);
            sum += combs_[i].process(in * 0.25f, t, 0.7f + size * 0.22f, 1.f) * 0.25f;
        }
        float a = 0.55f + size * 0.25f;
        sum = ap1_.process(sum, a);
        sum = ap2_.process(sum, -a);
        return lerp(in, sum, mix);
    }
private:
    ModDelay combs_[4];
    AllPass ap1_, ap2_;
    float sr_;
};

/** Cadeia multi-efeitos inspirada no Kaoss Pad, com 8 programas controlados por XY. */
class KaossChain {
public:
    explicit KaossChain(float sr)
        : sr_(sr), delay_(sr), chorus_(sr), flange_(sr),
          pitchA_(sr), pitchB_(sr), reverb_(sr) {}

    void setParams(int program, float x, float y, bool latch, float mix) {
        program_ = clamp(program, 0, 7);
        latch_ = latch;
        if (latch_) {
            // Congela o último valor quando latch está ligado; a UI já envia x/y travados.
            if (!latched_) { lx_ = x; ly_ = y; latched_ = true; }
            x_ = lx_; y_ = ly_;
        } else {
            latched_ = false; x_ = x; y_ = y;
        }
        mix_ = mix;
    }

    float process(float in) {
        float out = in;
        switch (program_) {
            case 0: { // Filter + Delay
                float cutoff = 180.f * std::pow(120.f, x_);
                svf_.process(out, cutoff, 1.f + y_ * 10.f, sr_);
                out = lerp(out, svf_.band + svf_.high * 0.5f, 0.85f);
                out = delay_.process(out, 20.f + y_ * 480.f, y_ * 0.92f, 0.55f);
                break;
            }
            case 1: { // Phaser + Reverb
                float rate = 0.1f + x_ * 8.f;
                phase_ += 6.2831853f * rate / sr_;
                float a = 0.45f + 0.4f * std::sin(phase_);
                float p = out;
                for (int i = 0; i < 4; ++i) p = ap_[i].process(p, a);
                out = lerp(out, p, 0.75f);
                out = reverb_.process(out, y_, 0.45f);
                break;
            }
            case 2: { // Flanger + Slicer
                phase_ += 6.2831853f * (0.2f + y_ * 12.f) / sr_;
                float t = 1.f + x_ * 9.f * (0.5f + 0.5f * std::sin(phase_));
                out = flange_.process(out, t, 0.72f, 0.7f);
                out *= slicer_(y_);
                break;
            }
            case 3: { // Chorus + Delay
                phase_ += 6.2831853f * (0.2f + x_ * 4.f) / sr_;
                float t = 8.f + 18.f * x_ * (0.5f + 0.5f * std::sin(phase_));
                out = chorus_.process(out, t, 0.2f, 0.55f);
                out = delay_.process(out, 120.f + y_ * 500.f, 0.55f, 0.5f);
                break;
            }
            case 4: { // Ring Mod + Filter
                ringPhase_ += 6.2831853f * (40.f * std::pow(80.f, x_)) / sr_;
                out *= std::sin(ringPhase_);
                svf_.process(out, 300.f + y_ * 7000.f, 3.f, sr_);
                out = svf_.low + svf_.high * 0.4f;
                break;
            }
            case 5: { // Pitch Shift + Reverb
                phase_ += 6.2831853f * (0.3f + x_ * 6.f) / sr_;
                float lfo = 0.5f + 0.5f * std::sin(phase_);
                float a = pitchA_.process(out, 4.f + lfo * 24.f, 0.f, 1.f);
                float b = pitchB_.process(out, 28.f - lfo * 24.f, 0.f, 1.f);
                out = lerp(a, b, lfo);
                out = reverb_.process(out, y_, 0.5f);
                break;
            }
            case 6: { // Tremolo + Distortion
                phase_ += 6.2831853f * (0.5f + x_ * 16.f) / sr_;
                out *= 0.5f + 0.5f * std::sin(phase_);
                out = fastTanh(out * (1.f + y_ * 12.f));
                break;
            }
            default: { // Vinyl + Gate
                if (rng_.next() > 0.998f - x_ * 0.004f) crack_ = 1.f;
                crack_ *= 0.88f;
                out += crack_ * rng_.next() * 0.6f;
                out *= slicer_(0.8f - x_ * 0.5f);
                out = lp_.process(out);
                break;
            }
        }
        return lerp(in, clamp(out, -1.5f, 1.5f), mix_);
    }

private:
    float slicer_(float y) {
        phase2_ += (0.5f + y * 12.f) / sr_;
        if (phase2_ >= 1.f) phase2_ -= 1.f;
        return phase2_ < (0.25f + y * 0.5f) ? 1.f : 0.05f;
    }

    float sr_;
    int program_ = 0;
    float x_ = 0.5f, y_ = 0.5f, mix_ = 1.f;
    bool latch_ = false, latched_ = false;
    float lx_ = 0.5f, ly_ = 0.5f;
    float phase_ = 0.f, phase2_ = 0.f, ringPhase_ = 0.f;
    float crack_ = 0.f;
    RNG rng_;
    SVF svf_;
    AllPass ap_[4];
    ModDelay delay_, chorus_, flange_, pitchA_, pitchB_;
    Reverb reverb_;
    OnePole lp_;
};
}
