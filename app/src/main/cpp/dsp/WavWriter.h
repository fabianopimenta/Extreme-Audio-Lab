#pragma once
#include <fstream>
#include <string>
#include <cstdint>
#include <cstring>

namespace hn {

/** Gravador WAV PCM 16-bit. Sem alocação por amostra. */
class WavWriter {
public:
    bool open(const std::string& path, int sampleRate, int channels) {
        file_.open(path, std::ios::binary | std::ios::trunc);
        if (!file_) return false;
        sr_ = sampleRate; ch_ = channels;
        writeHeader(0);
        return true;
    }

    void write(const float* interleaved, int frames) {
        if (!file_) return;
        for (int i = 0; i < frames * ch_; ++i) {
            int32_t v = (int32_t)(clamp(interleaved[i], -1.f, 1.f) * 32767.f);
            int16_t s = (int16_t)v;
            file_.write(reinterpret_cast<const char*>(&s), 2);
        }
        frames_ += frames;
    }

    void close() {
        if (!file_) return;
        file_.seekp(0);
        writeHeader(frames_);
        file_.close();
    }

    bool isOpen() const { return file_.is_open(); }

private:
    static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
    void writeHeader(uint32_t dataFrames) {
        uint32_t dataBytes = dataFrames * ch_ * 2;
        char h[44] = {};
        std::memcpy(h, "RIFF", 4);
        put32(h + 4, 36 + dataBytes);
        std::memcpy(h + 8, "WAVE", 4);
        std::memcpy(h + 12, "fmt ", 4);
        put32(h + 16, 16);
        put16(h + 20, 1);
        put16(h + 22, (uint16_t)ch_);
        put32(h + 24, (uint32_t)sr_);
        put32(h + 28, (uint32_t)(sr_ * ch_ * 2));
        put16(h + 32, (uint16_t)(ch_ * 2));
        put16(h + 34, 16);
        std::memcpy(h + 36, "data", 4);
        put32(h + 40, dataBytes);
        file_.write(h, 44);
    }
    static void put16(char* p, uint16_t v) { std::memcpy(p, &v, 2); }
    static void put32(char* p, uint32_t v) { std::memcpy(p, &v, 4); }

    std::ofstream file_;
    int sr_ = 48000, ch_ = 2;
    uint32_t frames_ = 0;
};
}
