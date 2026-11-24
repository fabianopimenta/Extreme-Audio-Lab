#pragma once
#include <cmath>
#include "Utils.h"

namespace hn {

/**
 * Destruction Lab — insert de destruição com 6 algoritmos.
 * 0 bypass, 1 hard clip, 2 asymmetric clip, 3 wavefold,
 * 4 bit reduction + sample & hold, 5 threshold gate.
 * Estado fixo, sem alocação na thread de áudio.
 */
class DestructionLab {
public:
    void setParams(int mode, float drive, float wet) {
        const int next = clampMode(mode);
        if (next != mode_) reset();
        mode_ = next;
        drive_ = clamp(drive, 1.f, 16.f);
        wet_ = clamp(wet, 0.f, 1.f);
    }

    void reset() {
        holdCounter_ = 0;
        held_ = 0.f;
    }

    float process(float x) {
        if (mode_ == 0 || wet_ <= 0.f) return x;
        const float v = x * drive_;
        float y = x;
        switch (mode_) {
            case 1: // hard clip
                y = clamp(v, -0.72f, 0.72f) / 0.72f;
                break;
            case 2: // asymmetric clip
                y = clamp(v + 0.16f, -0.48f, 0.72f) * 1.25f;
                break;
            case 3: { // foldback
                float f = std::fmod(v + 1.f, 4.f);
                if (f < 0.f) f += 4.f;
                if (f > 2.f) f = 4.f - f;
                y = (f - 1.f) * 0.85f;
                break;
            }
            case 4: { // 5-bit quantizer + sample & hold a cada 4 amostras
                if (holdCounter_ <= 0) {
                    held_ = std::round(clamp(v, -1.f, 1.f) * 15.f) / 15.f;
                    holdCounter_ = 3;
                } else {
                    --holdCounter_;
                }
                y = held_;
                break;
            }
            case 5: // threshold gate com transição suave
                y = std::fabs(v) < 0.10f ? 0.f : clamp(v, -0.85f, 0.85f);
                break;
            default:
                y = x;
        }
        return lerp(x, y, wet_);
    }

private:
    static int clampMode(int m) { return m < 0 ? 0 : (m > 5 ? 5 : m); }

    int mode_ = 0;
    float drive_ = 4.f;
    float wet_ = 0.f;
    int holdCounter_ = 0;
    float held_ = 0.f;
};

} // namespace hn
