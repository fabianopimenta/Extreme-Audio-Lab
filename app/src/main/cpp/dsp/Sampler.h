#pragma once
#include <algorithm>
#include <cmath>
#include <vector>
#include "Utils.h"

namespace hn {

/**
 * Player de uma amostra trazida pelo usuário (WAV).
 *
 * A amostra entra na MESMA mistura das outras fontes, antes do vocoder, do
 * glitch, do granular, do fuzz, do death metal, da destruição e do Kaoss.
 * Ou seja: ela é filtrada, quebrada e ressignificada por toda a cadeia.
 *
 * A thread de áudio é dona do buffer (`data_`). O carregamento acontece em
 * `AudioEngine::loadSample()` (thread da UI) e a troca do buffer é feita
 * dentro do callback, via `adopt()`.
 */
class Sampler {
public:
    void setEngineRate(float sr) { engineSr_ = sr > 1.f ? sr : 48000.f; }

    void setParams(float mix, float tuneSemis, float start01, bool loop) {
        mix_ = clamp(mix, 0.f, 1.f);
        tune_ = clamp(tuneSemis, -24.f, 24.f);
        start_ = clamp(start01, 0.f, 1.f);
        loop_ = loop;
    }

    /** Assume a posse de um buffer já pronto. Só chamar na thread de áudio. */
    void adopt(std::vector<float>&& data, float fileSr) {
        data_ = std::move(data);
        fileSr_ = (std::isfinite(fileSr) && fileSr > 1000.f) ? fileSr : engineSr_;
        pos_ = start_ * (float)data_.size();
        playing_ = !data_.empty();
    }

    void trigger() {
        if (data_.empty()) return;
        pos_ = start_ * (float)data_.size();
        playing_ = true;
    }

    void clear() {
        data_.clear();
        data_.shrink_to_fit();
        playing_ = false;
        pos_ = 0.f;
    }

    bool hasSample() const { return !data_.empty(); }
    std::size_t frames() const { return data_.size(); }

    float process() {
        const std::size_t n = data_.size();
        if (!playing_ || n == 0 || mix_ <= 0.0001f) return 0.f;
        const float rate = std::pow(2.f, tune_ / 12.f) * (fileSr_ / engineSr_);
        if (!std::isfinite(rate) || rate <= 0.f) return 0.f;
        if (pos_ < 0.f || pos_ >= (float)n) {
            if (loop_) pos_ = std::fmod(std::max(pos_, 0.f), (float)n);
            else { playing_ = false; return 0.f; }
        }
        const std::size_t i0 = (std::size_t)pos_;
        const std::size_t i1 = (i0 + 1 < n) ? (i0 + 1) : 0;
        const float frac = pos_ - (float)i0;
        float v = data_[i0] + (data_[i1] - data_[i0]) * frac;
        pos_ += rate;
        if (pos_ >= (float)n) {
            if (loop_) pos_ = std::fmod(pos_, (float)n);
            else { playing_ = false; pos_ = (float)n; }
        }
        if (!std::isfinite(v)) return 0.f;
        return clamp(v, -1.f, 1.f) * mix_;
    }

private:
    std::vector<float> data_;
    float engineSr_ = 48000.f;
    float fileSr_ = 48000.f;
    float mix_ = 0.f;
    float tune_ = 0.f;
    float start_ = 0.f;
    bool loop_ = true;
    float pos_ = 0.f;
    bool playing_ = false;
};

} // namespace hn
