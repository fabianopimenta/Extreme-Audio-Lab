#pragma once
#include <string>
#include <vector>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <unistd.h>
#include <cstring>
#include <cstdint>

namespace hn {

/** Envia PCM interleaved para outro dispositivo na mesma rede local (Wi-Fi Direct/hotspot). */
class UdpSender {
public:
    bool connect(const std::string& host, int port) {
        fd_ = socket(AF_INET, SOCK_DGRAM, 0);
        if (fd_ < 0) return false;
        std::memset(&addr_, 0, sizeof(addr_));
        addr_.sin_family = AF_INET;
        addr_.sin_port = htons((uint16_t)port);
        if (inet_pton(AF_INET, host.c_str(), &addr_.sin_addr) != 1) {
            close(fd_); fd_ = -1; return false;
        }
        connected_ = true;
        return true;
    }

    void send(const float* interleaved, int frames, int channels, int sampleRate) {
        if (!connected_) return;
        const int samples = frames * channels;
        const size_t size = 28 + samples * 2;
        if (size > buf_.size()) buf_.resize(size);
        uint32_t magic = 0x484E4C31; // HNL1
        uint32_t seq = seq_++;
        uint64_t ts = (uint64_t)(frames_ * 1'000'000LL / sampleRate);
        std::memcpy(buf_.data(), &magic, 4);
        std::memcpy(buf_.data() + 4, &seq, 4);
        std::memcpy(buf_.data() + 8, &ts, 8);
        std::memcpy(buf_.data() + 16, &frames, 4);
        std::memcpy(buf_.data() + 20, &channels, 4);
        std::memcpy(buf_.data() + 24, &sampleRate, 4);
        for (int i = 0; i < samples; ++i) {
            int32_t v = (int32_t)(clamp(interleaved[i], -1.f, 1.f) * 32767.f);
            int16_t s = (int16_t)v;
            std::memcpy(buf_.data() + 28 + i * 2, &s, 2);
        }
        sendto(fd_, buf_.data(), size, 0, (sockaddr*)&addr_, sizeof(addr_));
        frames_ += frames;
    }

    void disconnect() {
        if (fd_ >= 0) close(fd_);
        fd_ = -1; connected_ = false;
    }
    bool connected() const { return connected_; }
    ~UdpSender() { disconnect(); }

private:
    static float clamp(float v, float lo, float hi) { return v < lo ? lo : (v > hi ? hi : v); }
    int fd_ = -1;
    bool connected_ = false;
    sockaddr_in addr_{};
    uint32_t seq_ = 0;
    uint64_t frames_ = 0;
    std::vector<uint8_t> buf_;
};
}
