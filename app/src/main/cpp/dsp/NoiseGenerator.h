#pragma once
#include <vector>
#include "Utils.h"

namespace hn {

class NoiseGenerator {
public:
    explicit NoiseGenerator(float sr) : sr_(sr), comb_(2048, 0.f) {}

    void setParams(int type, float level, float crush, float holdHz, float comb) {
        type_ = type; level_ = level; crush_ = crush; holdHz_ = holdHz; comb_amt_ = comb;
    }

    float process() {
        float white = rng_.next();
        float out = white;
        switch (type_) {
            case 1: { // pink (Paul Kellet)
                b0_ = 0.99886f * b0_ + white * 0.0555179f;
                b1_ = 0.99332f * b1_ + white * 0.0750759f;
                b2_ = 0.96900f * b2_ + white * 0.1538520f;
                out = (b0_ + b1_ + b2_ + white * 0.5362f) * 0.11f;
                break;
            }
            case 2: // brown
                brown_ = clamp(brown_ + white * 0.02f, -1.f, 1.f);
                out = brown_ * 3.f;
                break;
            case 3: { // crackle
                if (rng_.next() > 0.995f) crack_ = 1.f;
                crack_ *= 0.92f;
                out = crack_ * rng_.next();
                break;
            }
            case 4: { // sample & hold
                if (++holdCount_ >= std::max(1.f, sr_ / std::max(1.f, holdHz_))) {
                    holdCount_ = 0; hold_ = white;
                }
                out = hold_;
                break;
            }
            default: break;
        }
        if (crush_ < 16.f) {
            float steps = std::pow(2.f, clamp(crush_, 2.f, 16.f) - 1.f);
            out = std::round(out * steps) / steps;
        }
        if (comb_amt_ > 0.f) {
            size_t len = (size_t)(2.f + comb_amt_ * 220.f);
            float d = comb_[combPos_];
            out = out + d * (0.35f + comb_amt_ * 0.6f);
            comb_[combPos_] = out;
            combPos_ = (combPos_ + 1) % comb_.size();
        }
        return out * level_;
    }

private:
    float sr_;
    RNG rng_;
    float b0_ = 0, b1_ = 0, b2_ = 0, brown_ = 0, crack_ = 0, hold_ = 0;
    int holdCount_ = 0;
    int type_ = 0;
    float level_ = 0.35f, crush_ = 16.f, holdHz_ = 40.f, comb_amt_ = 0.f;
    std::vector<float> comb_;
    size_t combPos_ = 0;
};
}
